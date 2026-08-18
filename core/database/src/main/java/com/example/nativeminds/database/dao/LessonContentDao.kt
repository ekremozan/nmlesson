package com.example.nativeminds.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.nativeminds.database.entity.LessonContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonContentDao {
    /**
     * Emits `null` while the lesson's text has never been stored, then the row once it is.
     *
     * That re-emission is what makes retry work with no second code path: a refresh writes the row
     * and an open reader fills itself.
     */
    @Query("SELECT * FROM lesson_content WHERE lessonId = :lessonId")
    fun observeContent(lessonId: Long): Flow<LessonContentEntity?>

    @Upsert
    suspend fun upsert(content: LessonContentEntity)

    @Upsert
    suspend fun upsertAll(content: List<LessonContentEntity>)
}
