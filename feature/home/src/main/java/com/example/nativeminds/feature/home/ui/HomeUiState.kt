package com.example.nativeminds.feature.home.ui

import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod

/**
 * The chrome [HomeScreen] needs around the story list — the list itself comes from
 * [HomeViewModel.pagedStories] via `collectAsLazyPagingItems()`, since paged content doesn't fit
 * a plain state snapshot.
 *
 * Carries data, not display strings: chrome text (greeting, section heading, counts) resolves to
 * a string resource in the Composable so the ViewModel stays free of Android/localization concerns.
 */
data class HomeUiState(
    val greeting: GreetingPeriod = GreetingPeriod.MORNING,
    val userName: String = "",
    val query: String = "",
    val chips: List<ChipUiModel> = emptyList(),
    val isFiltering: Boolean = false,
    val suggestions: List<ChipUiModel> = emptyList(),
)
