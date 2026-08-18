package com.example.nativeminds.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room's on-disk shape of a lesson — never passed to the domain or UI layer directly.
 *
 * `core:data` is where this converts to/from the domain `Lesson`.
 */
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: Long,
    val subject: String,
    val title: String,
    val teaser: String,
    val minutes: Int,
    val hasAudio: Boolean,
    val isLocked: Boolean,
    val image: String,
)
