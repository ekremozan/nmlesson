package com.example.nativeminds.data.remote

import com.example.nativeminds.data.local.DummyStorySeed
import com.example.nativeminds.model.Story
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * Stand-in for the real backend — returns the same seed catalog after a short simulated
 * round-trip. Cut corner: no backend exists yet; swapping this for a Retrofit/Ktor implementation
 * later changes nothing on [com.example.nativeminds.data.RoomStoryRepository]'s side.
 */
class FakeRemoteStoryDataSource @Inject constructor() : RemoteStoryDataSource {
    override suspend fun fetchStories(): List<Story> {
        delay(300)
        return DummyStorySeed.stories
    }
}
