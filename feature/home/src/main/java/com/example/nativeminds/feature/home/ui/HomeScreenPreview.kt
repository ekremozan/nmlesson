package com.example.nativeminds.feature.home.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.StoryUiModel
import com.example.nativeminds.feature.home.ui.preview.PreviewChips
import com.example.nativeminds.feature.home.ui.preview.PreviewStories
import com.example.nativeminds.feature.home.ui.preview.PreviewSuggestions
import kotlinx.coroutines.flow.flowOf

/**
 * Full-screen previews. These live in their own file (unlike the component previews, which sit
 * beside the composable) because they need the paging fixture wiring below.
 */

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
    suggestions = PreviewSuggestions,
)

// The screen previews pin a phone-sized canvas, so they spell the light/dark pair out instead of
// using @ThemePreviews (a multipreview can't carry a per-use size).
@Preview(name = "Home — light", showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun HomeScreenLightPreview() {
    NativeMindsTheme(darkTheme = false) {
        val stories = flowOf(PagingData.from(PreviewStories)).collectAsLazyPagingItems()
        HomeScreenContent(PreviewStateWithResults, stories, {}, {}, {}, {})
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
        HomeScreenContent(PreviewStateWithResults, stories, {}, {}, {}, {})
    }
}

@Preview(name = "Home — no results", showBackground = true, heightDp = 844, widthDp = 390)
@Composable
private fun HomeScreenNoResultsPreview() {
    NativeMindsTheme(darkTheme = false) {
        val stories = flowOf(PagingData.from(emptyList<StoryUiModel>())).collectAsLazyPagingItems()
        HomeScreenContent(PreviewStateNoResults, stories, {}, {}, {}, {})
    }
}
