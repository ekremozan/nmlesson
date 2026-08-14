package com.example.nativeminds.feature.home.ui.mapper

import com.example.nativeminds.feature.home.ui.model.LessonUiModel
import com.example.nativeminds.model.Lesson

fun Lesson.toUiModel(): LessonUiModel = LessonUiModel(
    id = id,
    subject = subject,
    title = title,
    teaser = teaser,
    minutesLabel = "$minutes min",
    hasAudio = hasAudio,
    isLocked = isLocked,
    image = image,
)
