package com.example.nativeminds.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * The readable payload of a lesson, kept in its own table rather than as columns on [LessonEntity].
 *
 * The lesson list is paged and re-queries on every keystroke; a body measured in kilobytes riding
 * along in those pages would undo the paging. Splitting it means the list touches metadata only
 * and the reader is the one place that pays for the text.
 *
 * [body] holds the whole lesson with a blank line between paragraphs — `core:data` is where that
 * becomes a structured list.
 */
@Entity(
    tableName = "lesson_content",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class LessonContentEntity(
    @PrimaryKey val lessonId: Long,
    val author: String,
    val body: String,
)
