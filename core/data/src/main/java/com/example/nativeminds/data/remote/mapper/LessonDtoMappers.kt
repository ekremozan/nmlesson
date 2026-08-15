package com.example.nativeminds.data.remote.mapper

import com.example.nativeminds.data.mapper.splitParagraphs
import com.example.nativeminds.data.remote.dto.LessonContentDto
import com.example.nativeminds.data.remote.dto.LessonDto
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent

fun LessonDto.toDomain(): Lesson = Lesson(
    id = id,
    subject = subject,
    title = title,
    teaser = teaser,
    minutes = minutes,
    hasAudio = hasAudio,
    isLocked = isLocked,
    image = image,
)

fun LessonContentDto.toDomain(): LessonContent = LessonContent(
    lessonId = lessonId,
    author = author,
    paragraphs = splitParagraphs(body),
)
