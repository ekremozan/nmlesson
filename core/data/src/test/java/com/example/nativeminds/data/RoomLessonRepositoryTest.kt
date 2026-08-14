package com.example.nativeminds.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.nativeminds.data.local.DummyLessonContentSeed
import com.example.nativeminds.data.local.DummyLessonSeed
import com.example.nativeminds.data.remote.RemoteLessonDataSource
import com.example.nativeminds.database.NativeMindsDatabase
import com.example.nativeminds.database.LessonEntity
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
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

private class SwitchableNetworkMonitor(var online: Boolean) : NetworkMonitor {
    override fun isOnline(): Boolean = online
}

private class CountingRemote : RemoteLessonDataSource {
    var lessonCalls = 0
        private set
    var contentCalls = 0
        private set

    override suspend fun fetchLessons(): List<Lesson> {
        lessonCalls++
        return DummyLessonSeed.lessons
    }

    override suspend fun fetchContent(lessonId: Long): LessonContent {
        contentCalls++
        return DummyLessonContentSeed.content.first { it.lessonId == lessonId }
    }
}

/**
 * Runs against a real in-memory Room database rather than fake DAOs: the guarantees being checked
 * here — that reading never reaches the network, and that a seeded lesson always has its text — are
 * properties of the queries and the transaction, which a fake DAO would simply assert about itself.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class RoomLessonRepositoryTest {
    private lateinit var database: NativeMindsDatabase
    private val network = SwitchableNetworkMonitor(online = false)
    private val remote = CountingRemote()
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
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedingWritesEveryLessonTogetherWithItsText() = runTest {
        repository.syncIfNeeded()

        val seeded = DummyLessonSeed.lessons.first()
        assertEquals(seeded.title, repository.lesson(seeded.id).first()?.title)
        val content = repository.lessonContent(seeded.id).first()
        assertEquals(seeded.id, content?.lessonId)
    }

    @Test
    fun readingALessonNeverReachesTheNetwork() = runTest {
        repository.syncIfNeeded()
        val callsAfterSeeding = remote.contentCalls

        repository.lesson(1).first()
        repository.lessonContent(1).first()

        assertEquals(callsAfterSeeding, remote.contentCalls)
        assertEquals(0, remote.lessonCalls)
    }

    @Test
    fun refreshingContentWhileOfflineFailsLoudly() = runTest {
        repository.syncIfNeeded()

        assertThrows(OfflineException::class.java) {
            runBlocking { repository.refreshContent(1) }
        }
    }

    @Test
    fun refreshingContentOnlineStoresItWhereReadingCanFindIt() = runTest {
        val lesson = DummyLessonSeed.lessons.first()
        database.lessonDao().upsertAll(
            listOf(
                LessonEntity(
                    id = lesson.id,
                    subject = lesson.subject,
                    title = lesson.title,
                    teaser = lesson.teaser,
                    minutes = lesson.minutes,
                    hasAudio = lesson.hasAudio,
                    isLocked = lesson.isLocked,
                    image = lesson.image,
                ),
            ),
        )
        assertNull(repository.lessonContent(lesson.id).first())
        network.online = true

        repository.refreshContent(lesson.id)

        val stored = repository.lessonContent(lesson.id).first()
        assertEquals(
            DummyLessonContentSeed.content.first { it.lessonId == lesson.id }.paragraphs,
            stored?.paragraphs,
        )
    }
}
