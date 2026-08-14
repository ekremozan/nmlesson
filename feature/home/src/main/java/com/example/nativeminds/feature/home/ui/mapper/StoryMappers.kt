package com.example.nativeminds.feature.home.ui.mapper

import com.example.nativeminds.feature.home.ui.model.StoryUiModel
import com.example.nativeminds.model.Story

fun Story.toUiModel(): StoryUiModel = StoryUiModel(
    id = id,
    category = category,
    title = title,
    teaser = teaser,
    minutesLabel = "$minutes min",
    hasAudio = hasAudio,
    isLocked = isLocked,
    image = image,
)
