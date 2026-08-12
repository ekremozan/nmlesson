package com.example.nativeminds.data.remote

import com.example.nativeminds.model.Story

/** The future real API. [FakeRemoteStoryDataSource] is the only implementation until it exists. */
interface RemoteStoryDataSource {
    suspend fun fetchStories(): List<Story>
}
