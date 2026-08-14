package com.example.nativeminds.data.remote

import com.example.nativeminds.data.local.DummyStoryContentSeed
import com.example.nativeminds.data.local.DummyStorySeed
import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.delay

private const val SIMULATED_ROUND_TRIP_MS = 300L

/**
 * Stand-in for the real backend — returns the same seed catalog after a short simulated
 * round-trip. Cut corner: no backend exists yet; swapping this for a Retrofit/Ktor implementation
 * later changes nothing on [com.example.nativeminds.data.RoomStoryRepository]'s side.
 */
class FakeRemoteStoryDataSource @Inject constructor() : RemoteStoryDataSource {
    override suspend fun fetchStories(): List<Story> {
        delay(SIMULATED_ROUND_TRIP_MS)
        return DummyStorySeed.stories
    }

    override suspend fun fetchContent(storyId: Long): StoryContent {
        delay(SIMULATED_ROUND_TRIP_MS)
        return DummyStoryContentSeed.content.firstOrNull { it.storyId == storyId }
            ?: throw IOException("No content for story $storyId")
    }
}
