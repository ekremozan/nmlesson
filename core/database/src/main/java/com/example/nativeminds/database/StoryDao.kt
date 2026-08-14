package com.example.nativeminds.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    /**
     * [category] `null` means "no category filter" — what the UI draws as the "All" chip. `null`
     * is the representation the whole stack uses; no layer translates a label into it.
     */
    @Query(
        """
        SELECT * FROM stories
        WHERE (:category IS NULL OR category = :category)
        AND (:query = '' OR title LIKE '%' || :query || '%' OR teaser LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
        ORDER BY id
        """,
    )
    fun pagingSource(category: String?, query: String): PagingSource<Int, StoryEntity>

    /**
     * The categories that actually have content, most-populated first. `GROUP BY` rather than
     * `DISTINCT` so `COUNT(*)` is available to order by; the alphabetical tie-break keeps the order
     * stable when two categories hold the same number of stories.
     *
     * A `Flow` because Room observes the table: a sync that introduces a new category updates the
     * filter row on its own, with nothing to keep in step by hand.
     */
    @Query("SELECT category FROM stories GROUP BY category ORDER BY COUNT(*) DESC, category ASC")
    fun categories(): Flow<List<String>>

    /** Emits `null` when the row is gone — a story deleted by a sync while the reader is open. */
    @Query("SELECT * FROM stories WHERE id = :id")
    fun observeStory(id: Long): Flow<StoryEntity?>

    @Upsert
    suspend fun upsertAll(stories: List<StoryEntity>)

    @Query("SELECT COUNT(*) FROM stories")
    suspend fun count(): Int
}
