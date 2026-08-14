package com.example.nativeminds.data.mapper

import com.example.nativeminds.database.StoryEntity
import com.example.nativeminds.model.Story

fun StoryEntity.toDomain(): Story = Story(
    id = id,
    category = category,
    title = title,
    teaser = teaser,
    minutes = minutes,
    hasAudio = hasAudio,
    isLocked = isLocked,
    image = image,
)

fun Story.toEntity(): StoryEntity = StoryEntity(
    id = id,
    category = category,
    title = title,
    teaser = teaser,
    minutes = minutes,
    hasAudio = hasAudio,
    isLocked = isLocked,
    image = image,
)
