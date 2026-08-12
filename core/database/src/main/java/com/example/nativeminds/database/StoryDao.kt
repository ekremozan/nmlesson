package com.example.nativeminds.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface StoryDao {

    /**
     * [category] `null` means "no category filter" (the UI's "All" chip) — the DAO stays free of
     * that UI-level string, the repository is what maps "All" to `null` before calling this.
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

    @Upsert
    suspend fun upsertAll(stories: List<StoryEntity>)

    @Query("SELECT COUNT(*) FROM stories")
    suspend fun count(): Int
}
