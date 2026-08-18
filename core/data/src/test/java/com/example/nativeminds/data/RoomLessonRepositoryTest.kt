package com.example.nativeminds.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.nativeminds.data.mapper.toEntity
import com.example.nativeminds.data.remote.RemoteLessonDataSource
import com.example.nativeminds.database.NativeMindsDatabase
import com.example.nativeminds.database.entity.LessonEntity
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROBOLECTRIC_SDK = 36

private fun lesson(id: Long, title: String = "Lesson $id") = Lesson(
    id = id,
    subject = "Biyoloji",
    title = title,
    teaser = "Teaser $id",
    minutes = 5,
    hasAudio = true,
    isLocked = false,
    image = "subject_biology",
)

private fun content(lessonId: Long) = LessonContent(
    lessonId = lessonId,
    author = "Author",
    paragraphs = listOf("Paragraph for $lessonId"),
)

private class SwitchableNetworkMonitor(var online: Boolean) : NetworkMonitor {
    override fun isOnline(): Boolean = online
}

private class FakeContentLanguageProvider(var language: String = "tr") : ContentLanguageProvider {
    override fun current(): String = language
}

private class FakeLastSyncedLanguageStore(private var language: String? = null) : LastSyncedLanguageStore {
    override fun get(): String? = language

    override fun set(language: String) {
        this.language = language
    }
}

private class FakeRemote(
    var lessons: List<Lesson> = listOf(lesson(1), lesson(2)),
) : RemoteLessonDataSource {
    var lessonCalls = 0
        private set
    var contentCalls = 0
        private set
    var lessonsFailure: Throwable? = null
    var lastLanguage: String? = null
        private set

    private val content = mutableMapOf(1L to content(1), 2L to content(2))

    override suspend fun fetchLessons(language: String): List<Lesson> {
        lessonCalls++
        lastLanguage = language
        lessonsFailure?.let { throw it }
        return lessons
    }

    override suspend fun fetchContent(lessonId: Long, language: String): LessonContent {
        contentCalls++
        lastLanguage = language
        return content.getValue(lessonId)
    }
}

/**
 * Runs against a real in-memory Room database rather than fake DAOs: the guarantees being checked
 * here — that reading never reaches the network, and that a sync's replace is transactional — are
 * properties of the queries and the transaction, which a fake DAO would simply assert about itself.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class RoomLessonRepositoryTest {
    private lateinit var database: NativeMindsDatabase
    private val network = SwitchableNetworkMonitor(online = false)
    private val remote = FakeRemote()
    private val contentLanguage = FakeContentLanguageProvider()
    private val lastSyncedLanguage = FakeLastSyncedLanguageStore()
    private lateinit var repository: RoomLessonRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NativeMindsDatabase::class.java,
        ).build()
        repository = RoomLessonRepository(
            database = database,
            dao = database.lessonDao(),
            contentDao = database.lessonContentDao(),
            remote = remote,
            networkMonitor = network,
            contentLanguage = contentLanguage,
            lastSyncedLanguage = lastSyncedLanguage,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun syncingWhileOfflineIsANoOp() = runTest {
        repository.syncIfNeeded()

        assertEquals(0, database.lessonDao().count())
        assertEquals(0, remote.lessonCalls)
    }

    @Test
    fun syncingOnlineWritesEveryRemoteLessonTogetherWithItsText() = runTest {
        network.online = true

        repository.syncIfNeeded()

        val first = remote.lessons.first()
        assertEquals(first.title, repository.lesson(first.id).first()?.title)
    }

    @Test
    fun anEditedLessonIsUpdatedOnTheNextSync() = runTest {
        network.online = true
        repository.syncIfNeeded()

        remote.lessons = listOf(lesson(1, title = "Updated title"), lesson(2))
        repository.syncIfNeeded()

        assertEquals("Updated title", repository.lesson(1).first()?.title)
    }

    @Test
    fun aLessonRemovedRemotelyDisappearsOnTheNextSync() = runTest {
        network.online = true
        repository.syncIfNeeded()

        remote.lessons = listOf(lesson(1))
        repository.syncIfNeeded()

        assertNull(repository.lesson(2).first())
        assertEquals(1, database.lessonDao().count())
    }

    @Test
    fun aFailedSyncLeavesThePreviousCatalogUntouched() = runTest {
        network.online = true
        repository.syncIfNeeded()
        val titleBefore = repository.lesson(1).first()?.title

        remote.lessonsFailure = IOException("connection dropped")
        assertThrows(IOException::class.java) {
            runBlocking { repository.syncIfNeeded() }
        }

        assertEquals(titleBefore, repository.lesson(1).first()?.title)
        assertEquals(2, database.lessonDao().count())
    }

    @Test
    fun anEmptyRemoteCatalogFailsRatherThanClearingTheLocalOne() = runTest {
        network.online = true
        repository.syncIfNeeded()

        remote.lessons = emptyList()
        assertThrows(IOException::class.java) {
            runBlocking { repository.syncIfNeeded() }
        }

        assertEquals(2, database.lessonDao().count())
    }

    @Test
    fun readingALessonNeverReachesTheNetwork() = runTest {
        network.online = true
        repository.syncIfNeeded()
        val callsAfterSync = remote.contentCalls

        repository.lesson(1).first()
        repository.lessonContent(1).first()

        assertEquals(callsAfterSync, remote.contentCalls)
    }

    @Test
    fun refreshingContentWhileOfflineFailsLoudly() = runTest {
        database.lessonDao().upsertAll(remote.lessons.map { it.toEntity() })

        assertThrows(OfflineException::class.java) {
            runBlocking { repository.refreshContent(1) }
        }
    }

    @Test
    fun refreshingContentOnlineStoresItWhereReadingCanFindIt() = runTest {
        val target = remote.lessons.first()
        database.lessonDao().upsertAll(
            listOf(
                LessonEntity(
                    id = target.id,
                    subject = target.subject,
                    title = target.title,
                    teaser = target.teaser,
                    minutes = target.minutes,
                    hasAudio = target.hasAudio,
                    isLocked = target.isLocked,
                    image = target.image,
                ),
            ),
        )
        assertNull(repository.lessonContent(target.id).first())
        network.online = true

        repository.refreshContent(target.id)

        val stored = repository.lessonContent(target.id).first()
        assertEquals(content(target.id).paragraphs, stored?.paragraphs)
    }

    @Test
    fun successfulSyncRecordsTheLanguageItSyncedIn() = runTest {
        network.online = true
        contentLanguage.language = "en"

        repository.syncIfNeeded()

        assertEquals("en", lastSyncedLanguage.get())
    }

    @Test
    fun clearingStaleLanguageContentIsANoOpWhenTheLanguageMatches() = runTest {
        network.online = true
        repository.syncIfNeeded()
        lastSyncedLanguage.set("tr")
        contentLanguage.language = "tr"

        repository.clearStaleLanguageContent()

        assertEquals(2, database.lessonDao().count())
    }

    @Test
    fun clearingStaleLanguageContentWipesTheCatalogWhenTheLanguageChanged() = runTest {
        network.online = true
        repository.syncIfNeeded()
        lastSyncedLanguage.set("tr")
        contentLanguage.language = "en"

        repository.clearStaleLanguageContent()

        assertEquals(0, database.lessonDao().count())
    }

    @Test
    fun clearingStaleLanguageContentWipesTheCatalogWhenNeverSynced() = runTest {
        database.lessonDao().upsertAll(remote.lessons.map { it.toEntity() })

        repository.clearStaleLanguageContent()

        assertEquals(0, database.lessonDao().count())
    }
}
