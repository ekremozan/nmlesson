package com.example.nativeminds.feature.home.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.feature.home.ui.HomeUiState
import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.StoryUiModel

/**
 * Fixtures shared by every `@Preview` in this feature.
 *
 * Kept in one place so a change to a UI model breaks one file instead of six, and so previews stay
 * static — they never reach the database or a ViewModel.
 */

internal val PreviewCategories = listOf("Fiction", "History", "Science", "Essays")

internal val PreviewChips = (listOf(null) + PreviewCategories)
    .mapIndexed { index, category -> ChipUiModel(category = category, isSelected = index == 0) }

internal val PreviewSuggestions = PreviewCategories.take(3).map { ChipUiModel(it) }

internal val PreviewStory = StoryUiModel(
    id = 1,
    category = "Fiction",
    title = "The Lighthouse Keeper's Last Letter",
    teaser = "Forty years of weather notes, and one page he never sent.",
    minutesLabel = "6 min",
    hasAudio = true,
    isLocked = false,
    image = "cover_01",
)

internal val PreviewLockedStory = StoryUiModel(
    id = 3,
    category = "History",
    title = "The Cartographer of Missing Islands",
    teaser = "For a century, the map showed land that was never there.",
    minutesLabel = "8 min",
    hasAudio = false,
    isLocked = true,
    image = "cover_03",
)

internal val PreviewStories = listOf(
    PreviewStory,
    StoryUiModel(2, "Science", "Why Bread Rises", "A single-celled organism, quietly doing all the work.", "4 min", hasAudio = true, isLocked = false, image = "cover_02"),
    PreviewLockedStory,
    StoryUiModel(4, "Essays", "On Walking Slowly", "What a city gives back when you stop trying to cross it.", "5 min", hasAudio = true, isLocked = false, image = "cover_04"),
)

/** One row of the home screen's preview matrix: a state and the stories the pager returns for it. */
internal data class HomePreviewCase(
    val state: HomeUiState,
    val stories: List<StoryUiModel>,
)

/** Content states of the home screen. The theme axis comes from `@ScreenThemePreviews`. */
internal class HomePreviewCases : PreviewParameterProvider<HomePreviewCase> {
    override val values = sequenceOf(
        HomePreviewCase(
            state = HomeUiState(
                greeting = GreetingPeriod.MORNING,
                userName = "Ozan",
                categories = PreviewCategories,
            ),
            stories = PreviewStories,
        ),
        HomePreviewCase(
            state = HomeUiState(
                greeting = GreetingPeriod.MORNING,
                userName = "Ozan",
                query = "quantum lullabies",
                categories = PreviewCategories,
            ),
            stories = emptyList(),
        ),
    )
}
