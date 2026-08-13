package com.example.nativeminds.feature.home.ui

import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod

/** How many category chips the empty state has room for. */
private const val SUGGESTION_COUNT = 3

/**
 * The chrome [HomeScreen] needs around the story list — the list itself comes from
 * [HomeViewModel.pagedStories] via `collectAsLazyPagingItems()`, since paged content doesn't fit
 * a plain state snapshot.
 *
 * Carries data, not display strings: chrome text (greeting, section heading, counts) resolves to
 * a string resource in the Composable so the state stays free of Android/localization concerns.
 */
data class HomeUiState(
    val greeting: GreetingPeriod = GreetingPeriod.MORNING,
    val userName: String = "",
    val query: String = "",
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
) {
    val chips: List<ChipUiModel>
        get() = (listOf(null) + categories).map { category ->
            ChipUiModel(category = category, isSelected = category == selectedCategory)
        }

    val suggestions: List<ChipUiModel>
        get() = categories.take(SUGGESTION_COUNT).map { ChipUiModel(it) }

    val isFiltering: Boolean
        get() = query.isNotEmpty() || selectedCategory != null
}

sealed interface HomeIntent {
    data class QueryChanged(val query: String) : HomeIntent

    data object QueryCleared : HomeIntent

    data class CategorySelected(val category: String?) : HomeIntent

    data class SuggestionSelected(val category: String?) : HomeIntent

    data class CategoriesLoaded(val categories: List<String>) : HomeIntent
}

sealed interface HomeEffect {
    data object ShowSyncError : HomeEffect
}
