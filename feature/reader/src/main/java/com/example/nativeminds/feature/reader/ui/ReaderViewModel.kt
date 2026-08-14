package com.example.nativeminds.feature.reader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.nativeminds.domain.usecase.ObserveStoryDetailUseCase
import com.example.nativeminds.feature.reader.navigation.ReaderRoute
import dagger.hilt.android.lifecycle.HiltViewModel
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

/**
 * What the content flow is keyed on. A retry changes [retryToken], which changes the key, which
 * re-subscribes — so retry is a pure state transition rather than an imperative reload call.
 */
private data class LoadKey(val storyId: Long, val retryToken: Int)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeStoryDetail: ObserveStoryDetailUseCase,
) : ViewModel() {
    private val storyId = savedStateHandle.toRoute<ReaderRoute>().storyId

    private val _state = MutableStateFlow(ReaderUiState(storyId = storyId))

    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private val effectChannel = Channel<ReaderEffect>(Channel.BUFFERED)

    val effects: Flow<ReaderEffect> = effectChannel.receiveAsFlow()

    init {
        _state
            .map { LoadKey(it.storyId, it.retryToken) }
            .distinctUntilChanged()
            .flatMapLatest { key -> observeStoryDetail(key.storyId) }
            .onEach { onIntent(ReaderIntent.DetailChanged(it)) }
            .launchIn(viewModelScope)
    }

    /**
     * The whole ViewModel's decision-making, and there is none: the reducer decides what the next
     * state is and which effects the intent raised, and this applies both. A `when (intent)` here
     * would be a second place that knows what intents mean.
     */
    fun onIntent(intent: ReaderIntent) {
        val reduction = _state.value.reduce(intent)
        _state.value = reduction.state
        reduction.effects.forEach(effectChannel::trySend)
    }
}
