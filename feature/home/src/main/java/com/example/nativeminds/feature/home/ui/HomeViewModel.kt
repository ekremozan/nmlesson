package com.example.nativeminds.feature.home.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.nativeminds.data.StoryRepository
import com.example.nativeminds.data.StoryRepositoryProvider
import com.example.nativeminds.feature.home.ui.mapper.toUiModel
import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.StoryUiModel
import java.util.Calendar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val ALL_CATEGORY = "All"
private val CATEGORIES = listOf(ALL_CATEGORY, "Fiction", "History", "Science", "Essays")
private val SEARCH_SUGGESTIONS = listOf("Fiction", "Science", "Essays")

private data class FilterParams(val category: String = ALL_CATEGORY, val query: String = "")

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: StoryRepository,
) : ViewModel() {

    private val filterParams = MutableStateFlow(FilterParams())

    private val _uiState = MutableStateFlow(HomeUiState(userName = "Ozan", greeting = greetingForNow()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val pagedStories: Flow<PagingData<StoryUiModel>> = filterParams
        .flatMapLatest { params ->
            val category = params.category.takeUnless { it == ALL_CATEGORY }
            repository.pagedStories(category, params.query.trim())
        }
        .map { pagingData -> pagingData.map { it.toUiModel() } }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch { repository.syncIfNeeded() }
        refreshChrome()
    }

    fun onQueryChange(newQuery: String) {
        filterParams.value = filterParams.value.copy(query = newQuery)
        refreshChrome()
    }

    fun onClearQuery() {
        filterParams.value = FilterParams()
        refreshChrome()
    }

    fun onCategorySelected(category: String) {
        filterParams.value = filterParams.value.copy(category = category)
        refreshChrome()
    }

    /** Updates the chrome (search text, chips) that doesn't come from the Pager. */
    private fun refreshChrome() {
        val params = filterParams.value
        _uiState.value = _uiState.value.copy(
            query = params.query,
            chips = CATEGORIES.map { ChipUiModel(label = it, isSelected = it == params.category) },
            isFiltering = params.query.isNotEmpty() || params.category != ALL_CATEGORY,
            suggestions = SEARCH_SUGGESTIONS.map { ChipUiModel(label = it) },
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                HomeViewModel(StoryRepositoryProvider.create(app))
            }
        }
    }
}

private fun greetingForNow(): GreetingPeriod = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> GreetingPeriod.MORNING
    in 12..17 -> GreetingPeriod.AFTERNOON
    else -> GreetingPeriod.EVENING
}
