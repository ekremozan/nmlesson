package com.example.nativeminds.data.mapper

import com.example.nativeminds.database.LessonContentEntity
import com.example.nativeminds.model.LessonContent

private const val PARAGRAPH_SEPARATOR = "\n\n"

fun LessonContentEntity.toDomain(): LessonContent = LessonContent(
    lessonId = lessonId,
    author = author,
    paragraphs = body.split(PARAGRAPH_SEPARATOR)
        .map { it.trim() }
        .filter { it.isNotEmpty() },
)

fun LessonContent.toEntity(): LessonContentEntity = LessonContentEntity(
    lessonId = lessonId,
    author = author,
    body = paragraphs.joinToString(PARAGRAPH_SEPARATOR),
)
