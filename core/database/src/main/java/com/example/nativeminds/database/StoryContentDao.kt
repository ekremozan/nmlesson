package com.example.nativeminds.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryContentDao {
    /**
     * Emits `null` while the story's text has never been stored, then the row once it is.
     *
     * That re-emission is what makes retry work with no second code path: a refresh writes the row
     * and an open reader fills itself.
     */
    @Query("SELECT * FROM story_content WHERE storyId = :storyId")
    fun observeContent(storyId: Long): Flow<StoryContentEntity?>

    @Upsert
    suspend fun upsert(content: StoryContentEntity)

    @Upsert
    suspend fun upsertAll(content: List<StoryContentEntity>)
}
