package com.example.nativeminds.data

import androidx.paging.PagingData
import com.example.nativeminds.model.Story
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for stories — Room-backed, paged. [RoomStoryRepository] is the only
 * implementation; swapping the remote data source later touches nothing above this interface.
 */
interface StoryRepository {
    /** [category] `null` means unfiltered ("All"). */
    fun pagedStories(category: String?, query: String): Flow<PagingData<Story>>

    /**
     * Seeds the local database on first run and, if online, refreshes it from the remote source.
     * Safe to call every time the app starts — it's a no-op past the first run unless online.
     */
    suspend fun syncIfNeeded()
}
