package com.example.nativeminds.feature.reader.ui

import com.example.nativeminds.domain.model.ReaderAccess
import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent

internal val unlockedStory = Story(
    id = 1,
    category = "Fiction",
    title = "The Lighthouse Keeper's Last Letter",
    teaser = "One page he never sent.",
    minutes = 6,
    hasAudio = true,
    isLocked = false,
)

internal val lockedStory = unlockedStory.copy(
    id = 3,
    category = "History",
    title = "The Cartographer of Missing Islands",
    isLocked = true,
)

internal val storyContent = StoryContent(
    storyId = unlockedStory.id,
    author = "Marguerite Halloran",
    paragraphs = listOf("First.", "Second.", "Third."),
)

internal val fullAccess = ReaderAccess.Full(unlockedStory, storyContent)

internal val previewAccess = ReaderAccess.Preview(
    story = lockedStory,
    author = "Tomás Ferreiro",
    paragraphs = listOf("First."),
    freeSharePercent = 30,
)

internal fun initialState() = ReaderUiState(storyId = unlockedStory.id)
