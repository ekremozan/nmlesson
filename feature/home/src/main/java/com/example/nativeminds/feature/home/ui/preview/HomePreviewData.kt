package com.example.nativeminds.feature.home.ui.preview

import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.StoryUiModel

/**
 * Fixtures shared by every `@Preview` in this feature.
 *
 * Kept in one place so a change to a UI model breaks one file instead of six, and so previews stay
 * static — they never reach the database or a ViewModel.
 */

// Leading null is the "All" chip.
internal val PreviewChips = listOf(null, "Fiction", "History", "Science", "Essays")
    .mapIndexed { index, category -> ChipUiModel(category = category, isSelected = index == 0) }

internal val PreviewSuggestions = listOf("Fiction", "Science", "Essays").map { ChipUiModel(it) }

internal val PreviewStory = StoryUiModel(
    id = 1,
    category = "Fiction",
    title = "The Lighthouse Keeper's Last Letter",
    teaser = "Forty years of weather notes, and one page he never sent.",
    minutesLabel = "6 min",
    hasAudio = true,
    isLocked = false,
)

internal val PreviewLockedStory = StoryUiModel(
    id = 3,
    category = "History",
    title = "The Cartographer of Missing Islands",
    teaser = "For a century, the map showed land that was never there.",
    minutesLabel = "8 min",
    hasAudio = false,
    isLocked = true,
)

internal val PreviewStories = listOf(
    PreviewStory,
    StoryUiModel(2, "Science", "Why Bread Rises", "A single-celled organism, quietly doing all the work.", "4 min", hasAudio = true, isLocked = false),
    PreviewLockedStory,
    StoryUiModel(4, "Essays", "On Walking Slowly", "What a city gives back when you stop trying to cross it.", "5 min", hasAudio = true, isLocked = false),
)
