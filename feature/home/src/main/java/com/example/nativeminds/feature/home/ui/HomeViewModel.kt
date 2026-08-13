package com.example.nativeminds.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.nativeminds.domain.usecase.GetCategoriesUseCase
import com.example.nativeminds.domain.usecase.GetPagedStoriesUseCase
import com.example.nativeminds.domain.usecase.SyncStoriesUseCase
import com.example.nativeminds.feature.home.ui.mapper.toUiModel
import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.StoryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How many category chips the empty state has room for. */
private const val SUGGESTION_COUNT = 3
private const val USER_NAME = "Ozan"

/** [category] `null` means no category filter — the "All" chip. */
private data class FilterParams(val category: String? = null, val query: String = "")

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    getCategories: GetCategoriesUseCase,
    private val getPagedStories: GetPagedStoriesUseCase,
    private val syncStories: SyncStoriesUseCase,
) : ViewModel() {

    private val filterParams = MutableStateFlow(FilterParams())

    /** Fixed for the lifetime of the screen, so the header doesn't change under the user at 12:00. */
    private val greeting = greetingForNow()

    /**
     * Derived rather than assigned: the chips follow whatever categories the database currently
     * holds, so a sync that adds or empties one is reflected without anything to keep in step.
     */
    val uiState: StateFlow<HomeUiState> = combine(filterParams, getCategories()) { params, categories ->
        HomeUiState(
            greeting = greeting,
            userName = USER_NAME,
            query = params.query,
            // The leading null is the "All" chip — a UI affordance, not a category in the data.
            chips = (listOf(null) + categories).map { category ->
                ChipUiModel(category = category, isSelected = category == params.category)
            },
            isFiltering = params.query.isNotEmpty() || params.category != null,
            // Already ordered by story count, so the first few are the fullest shelves.
            suggestions = categories.take(SUGGESTION_COUNT).map { ChipUiModel(category = it) },
        )
    }.stateIn(
        scope = viewModelScope,
        // Stops observing Room while the screen is backgrounded, and survives a config change.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(greeting = greeting, userName = USER_NAME),
    )

    val pagedStories: Flow<PagingData<StoryUiModel>> = filterParams
        .flatMapLatest { params -> getPagedStories(params.category, params.query.trim()) }
        .map { pagingData -> pagingData.map { it.toUiModel() } }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch { syncStories() }
    }

    fun onQueryChange(newQuery: String) {
        filterParams.value = filterParams.value.copy(query = newQuery)
    }

    fun onClearQuery() {
        filterParams.value = FilterParams()
    }

    /** [category] `null` is the "All" chip. Keeps the query — filtering within a search is valid. */
    fun onCategorySelected(category: String?) {
        filterParams.value = filterParams.value.copy(category = category)
    }

    /**
     * A suggestion from the empty state, which means "show me this category instead". The failed
     * query has to go with it — narrowing a search that already matched nothing by category can
     * only ever produce the same empty screen the user is trying to leave.
     */
    fun onSuggestionSelected(category: String?) {
        filterParams.value = FilterParams(category = category)
    }
}

private fun greetingForNow(): GreetingPeriod = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> GreetingPeriod.MORNING
    in 12..17 -> GreetingPeriod.AFTERNOON
    else -> GreetingPeriod.EVENING
}
