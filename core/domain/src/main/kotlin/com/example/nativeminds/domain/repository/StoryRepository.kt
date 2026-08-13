package com.example.nativeminds.domain.repository

import androidx.paging.PagingData
import com.example.nativeminds.model.Story
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for stories — Room-backed, paged.
 *
 * The interface lives in the domain layer and the implementation in `:core:data`, so the
 * dependency arrow points inwards: feature modules depend on this contract and never on the
 * concrete data module. Swapping the remote data source, or Room itself, touches nothing above it.
 */
interface StoryRepository {
    /** [category] `null` means unfiltered ("All"). */
    fun pagedStories(category: String?, query: String): Flow<PagingData<Story>>

    /**
     * Every category that currently has stories, ordered by how much content it holds. Derived
     * from the stories themselves rather than kept as a second list, so it cannot drift out of
     * step with them.
     */
    fun categories(): Flow<List<String>>

    /**
     * Seeds the local database on first run and, if online, refreshes it from the remote source.
     * Safe to call every time the app starts — it's a no-op past the first run unless online.
     */
    suspend fun syncIfNeeded()
}
