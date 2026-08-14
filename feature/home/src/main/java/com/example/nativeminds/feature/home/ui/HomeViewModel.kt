package com.example.nativeminds.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.nativeminds.domain.usecase.GetPagedLessonsUseCase
import com.example.nativeminds.domain.usecase.GetSubjectsUseCase
import com.example.nativeminds.domain.usecase.SyncLessonsUseCase
import com.example.nativeminds.feature.home.ui.mapper.toUiModel
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.LessonUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val USER_NAME = "Ozan"

private data class FilterParams(val subject: String?, val query: String)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    getSubjects: GetSubjectsUseCase,
    getPagedLessons: GetPagedLessonsUseCase,
    private val syncLessons: SyncLessonsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(
        HomeUiState(greeting = greetingForNow(), userName = USER_NAME),
    )

    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)

    val effects: Flow<HomeEffect> = effectChannel.receiveAsFlow()

    val pagedLessons: Flow<PagingData<LessonUiModel>> = _state
        .map { FilterParams(it.selectedSubject, it.query.trim()) }
        .distinctUntilChanged()
        .flatMapLatest { params -> getPagedLessons(params.subject, params.query) }
        .map { pagingData -> pagingData.map { it.toUiModel() } }
        .cachedIn(viewModelScope)

    init {
        getSubjects()
            .onEach { onIntent(HomeIntent.SubjectsLoaded(it)) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            runCatching { syncLessons() }
                .onFailure { effectChannel.send(HomeEffect.ShowSyncError) }
        }
    }

    fun onIntent(intent: HomeIntent) = _state.update { it.reduce(intent) }
}

private fun greetingForNow(): GreetingPeriod = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> GreetingPeriod.MORNING
    in 12..17 -> GreetingPeriod.AFTERNOON
    else -> GreetingPeriod.EVENING
}
