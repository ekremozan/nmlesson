package com.example.nativeminds.data.remote

import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent

/** The future real API. [FakeRemoteStoryDataSource] is the only implementation until it exists. */
interface RemoteStoryDataSource {
    suspend fun fetchStories(): List<Story>

    /**
     * The text of one story. Fetched on demand rather than with the catalog, because a list of
     * cards has no use for six full stories.
     *
     * Throws when the story is unknown to the source — the caller turns that into something the
     * reader can see.
     */
    suspend fun fetchContent(storyId: Long): StoryContent
}
