package com.example.nativeminds.data.mapper

import com.example.nativeminds.database.LessonEntity
import com.example.nativeminds.model.Lesson

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    subject = subject,
    title = title,
    teaser = teaser,
    minutes = minutes,
    hasAudio = hasAudio,
    isLocked = isLocked,
    image = image,
)

fun Lesson.toEntity(): LessonEntity = LessonEntity(
    id = id,
    subject = subject,
    title = title,
    teaser = teaser,
    minutes = minutes,
    hasAudio = hasAudio,
    isLocked = isLocked,
    image = image,
)
