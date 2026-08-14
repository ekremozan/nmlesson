package com.example.nativeminds.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room's on-disk shape of a story — never passed to the domain or UI layer directly.
 *
 * `core:data` is where this converts to/from the domain `Story`.
 */
@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: Long,
    val category: String,
    val title: String,
    val teaser: String,
    val minutes: Int,
    val hasAudio: Boolean,
    val isLocked: Boolean,
    val image: String,
)
