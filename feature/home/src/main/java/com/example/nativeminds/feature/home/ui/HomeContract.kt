package com.example.nativeminds.feature.home.ui

import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod

/** How many subject chips the empty state has room for. */
private const val SUGGESTION_COUNT = 3

/**
 * The chrome [HomeScreen] needs around the lesson list — the list itself comes from
 * [HomeViewModel.pagedLessons] via `collectAsLazyPagingItems()`, since paged content doesn't fit
 * a plain state snapshot.
 *
 * Carries data, not display strings: chrome text (greeting, section heading, counts) resolves to
 * a string resource in the Composable so the state stays free of Android/localization concerns.
 */
data class HomeUiState(
    val greeting: GreetingPeriod = GreetingPeriod.MORNING,
    val userName: String = "",
    val query: String = "",
    val selectedSubject: String? = null,
    val subjects: List<String> = emptyList(),
    val syncToken: Int = 0,
    /**
     * `false` until the local catalog's language has been checked against the current content
     * language, once, on creation. [HomeViewModel.pagedLessons] gates on this so a device-language
     * switch can never render a lesson cached in the previous language, even for one frame.
     */
    val contentVerified: Boolean = false,
) {
    val chips: List<ChipUiModel>
        get() = (listOf(null) + subjects).map { subject ->
            ChipUiModel(subject = subject, isSelected = subject == selectedSubject)
        }

    val suggestions: List<ChipUiModel>
        get() = subjects.take(SUGGESTION_COUNT).map { ChipUiModel(it) }

    val isFiltering: Boolean
        get() = query.isNotEmpty() || selectedSubject != null
}

sealed interface HomeIntent {
    data class QueryChanged(val query: String) : HomeIntent

    data object QueryCleared : HomeIntent

    data class SubjectSelected(val subject: String?) : HomeIntent

    data class SuggestionSelected(val subject: String?) : HomeIntent

    data class SubjectsLoaded(val subjects: List<String>) : HomeIntent

    /** Pull-to-refresh. The reduction only bumps [HomeUiState.syncToken]. */
    data object RefreshRequested : HomeIntent

    /** The one-time startup language check has finished. Unblocks [HomeUiState.contentVerified]. */
    data object ContentLanguageVerified : HomeIntent
}

sealed interface HomeEffect {
    data object ShowSyncError : HomeEffect
}
