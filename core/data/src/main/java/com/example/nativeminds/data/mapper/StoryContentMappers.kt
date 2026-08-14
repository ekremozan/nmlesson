package com.example.nativeminds.data.mapper

import com.example.nativeminds.database.StoryContentEntity
import com.example.nativeminds.model.StoryContent

private const val PARAGRAPH_SEPARATOR = "\n\n"

fun StoryContentEntity.toDomain(): StoryContent = StoryContent(
    storyId = storyId,
    author = author,
    paragraphs = body.split(PARAGRAPH_SEPARATOR)
        .map { it.trim() }
        .filter { it.isNotEmpty() },
)

fun StoryContent.toEntity(): StoryContentEntity = StoryContentEntity(
    storyId = storyId,
    author = author,
    body = paragraphs.joinToString(PARAGRAPH_SEPARATOR),
)
