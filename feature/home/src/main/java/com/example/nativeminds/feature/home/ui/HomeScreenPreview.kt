package com.example.nativeminds.feature.home.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.StoryUiModel
import kotlinx.coroutines.flow.flowOf

private val PreviewChips = listOf("All", "Fiction", "History", "Science", "Essays")
    .mapIndexed { index, label -> ChipUiModel(label = label, isSelected = index == 0) }

private val PreviewStories = listOf(
    StoryUiModel(1, "Fiction", "The Lighthouse Keeper's Last Letter", "Forty years of weather notes, and one page he never sent.", "6 min", hasAudio = true, isLocked = false),
    StoryUiModel(2, "Science", "Why Bread Rises", "A single-celled organism, quietly doing all the work.", "4 min", hasAudio = true, isLocked = false),
    StoryUiModel(3, "History", "The Cartographer of Missing Islands", "For a century, the map showed land that was never there.", "8 min", hasAudio = false, isLocked = true),
    StoryUiModel(4, "Essays", "On Walking Slowly", "What a city gives back when you stop trying to cross it.", "5 min", hasAudio = true, isLocked = false),
)

private val PreviewStateWithResults = HomeUiState(
    greeting = GreetingPeriod.MORNING,
    userName = "Ozan",
    chips = PreviewChips,
)

private val PreviewStateNoResults = HomeUiState(
    greeting = GreetingPeriod.MORNING,
    userName = "Ozan",
    query = "quantum lullabies",
    chips = PreviewChips,
    isFiltering = true,
    suggestions = listOf("Fiction", "Science", "Essays").map { ChipUiModel(it) },
)

@Preview(name = "Home — light", showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun HomeScreenLightPreview() {
    NativeMindsTheme(darkTheme = false) {
        val stories = flowOf(PagingData.from(PreviewStories)).collectAsLazyPagingItems()
        HomeScreenContent(PreviewStateWithResults, stories, {}, {}, {})
    }
}

@Preview(
    name = "Home — dark",
    showBackground = true,
    heightDp = 844,
    widthDp = 390,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeScreenDarkPreview() {
    NativeMindsTheme(darkTheme = true) {
        val stories = flowOf(PagingData.from(PreviewStories)).collectAsLazyPagingItems()
        HomeScreenContent(PreviewStateWithResults, stories, {}, {}, {})
    }
}

@Preview(name = "Home — no results", showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun HomeScreenNoResultsPreview() {
    NativeMindsTheme(darkTheme = false) {
        val stories = flowOf(PagingData.from(emptyList<StoryUiModel>())).collectAsLazyPagingItems()
        HomeScreenContent(PreviewStateNoResults, stories, {}, {}, {})
    }
}
