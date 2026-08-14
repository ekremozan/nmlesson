package com.example.nativeminds.domain.repository

import androidx.paging.PagingData
import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent
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

    /** One story, re-emitted on every change. `null` once the row is no longer stored locally. */
    fun story(id: Long): Flow<Story?>

    /**
     * The story's text, re-emitted once a refresh writes it. `null` means "never stored" — not an
     * error, just a story this device has not read yet.
     *
     * Reads never touch the network, which is what makes offline reading a property of the read
     * path rather than a fallback branch someone has to remember to write.
     */
    fun storyContent(id: Long): Flow<StoryContent?>

    /**
     * Fetches the story's text and stores it.
     *
     * **Throws** when offline or when the fetch fails, rather than returning quietly with nothing:
     * the caller is the one that knows how to turn a failure into something the reader can see and
     * something the error reporter records.
     */
    suspend fun refreshContent(id: Long)

    /**
     * Seeds the local database on first run and, if online, refreshes it from the remote source.
     * Safe to call every time the app starts — it's a no-op past the first run unless online.
     */
    suspend fun syncIfNeeded()
}
