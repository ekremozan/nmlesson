package com.example.nativeminds.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * The readable payload of a story, kept in its own table rather than as columns on [StoryEntity].
 *
 * The story list is paged and re-queries on every keystroke; a body measured in kilobytes riding
 * along in those pages would undo the paging. Splitting it means the list touches metadata only
 * and the reader is the one place that pays for the text.
 *
 * [body] holds the whole story with a blank line between paragraphs — `core:data` is where that
 * becomes a structured list.
 */
@Entity(
    tableName = "story_content",
    foreignKeys = [
        ForeignKey(
            entity = StoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["storyId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StoryContentEntity(
    @PrimaryKey val storyId: Long,
    val author: String,
    val body: String,
)
