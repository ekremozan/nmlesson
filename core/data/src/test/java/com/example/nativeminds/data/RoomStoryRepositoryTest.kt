package com.example.nativeminds.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.nativeminds.data.local.DummyStoryContentSeed
import com.example.nativeminds.data.local.DummyStorySeed
import com.example.nativeminds.data.remote.RemoteStoryDataSource
import com.example.nativeminds.database.NativeMindsDatabase
import com.example.nativeminds.database.StoryEntity
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent
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

private class CountingRemote : RemoteStoryDataSource {
    var storyCalls = 0
        private set
    var contentCalls = 0
        private set

    override suspend fun fetchStories(): List<Story> {
        storyCalls++
        return DummyStorySeed.stories
    }

    override suspend fun fetchContent(storyId: Long): StoryContent {
        contentCalls++
        return DummyStoryContentSeed.content.first { it.storyId == storyId }
    }
}

/**
 * Runs against a real in-memory Room database rather than fake DAOs: the guarantees being checked
 * here — that reading never reaches the network, and that a seeded story always has its text — are
 * properties of the queries and the transaction, which a fake DAO would simply assert about itself.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class RoomStoryRepositoryTest {
    private lateinit var database: NativeMindsDatabase
    private val network = SwitchableNetworkMonitor(online = false)
    private val remote = CountingRemote()
    private lateinit var repository: RoomStoryRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NativeMindsDatabase::class.java,
        ).build()
        repository = RoomStoryRepository(
            database = database,
            dao = database.storyDao(),
            contentDao = database.storyContentDao(),
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
    fun seedingWritesEveryStoryTogetherWithItsText() = runTest {
        repository.syncIfNeeded()

        val seeded = DummyStorySeed.stories.first()
        assertEquals(seeded.title, repository.story(seeded.id).first()?.title)
        val content = repository.storyContent(seeded.id).first()
        assertEquals(seeded.id, content?.storyId)
    }

    @Test
    fun readingAStoryNeverReachesTheNetwork() = runTest {
        repository.syncIfNeeded()
        val callsAfterSeeding = remote.contentCalls

        repository.story(1).first()
        repository.storyContent(1).first()

        assertEquals(callsAfterSeeding, remote.contentCalls)
        assertEquals(0, remote.storyCalls)
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
        val story = DummyStorySeed.stories.first()
        database.storyDao().upsertAll(
            listOf(
                StoryEntity(
                    id = story.id,
                    category = story.category,
                    title = story.title,
                    teaser = story.teaser,
                    minutes = story.minutes,
                    hasAudio = story.hasAudio,
                    isLocked = story.isLocked,
                    image = story.image,
                ),
            ),
        )
        assertNull(repository.storyContent(story.id).first())
        network.online = true

        repository.refreshContent(story.id)

        val stored = repository.storyContent(story.id).first()
        assertEquals(
            DummyStoryContentSeed.content.first { it.storyId == story.id }.paragraphs,
            stored?.paragraphs,
        )
    }
}
