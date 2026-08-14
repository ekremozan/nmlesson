package com.example.nativeminds.feature.reader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.nativeminds.domain.narration.StoryNarrator
import com.example.nativeminds.domain.usecase.ObserveNarrationUseCase
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
    observeNarration: ObserveNarrationUseCase,
    private val storyNarrator: StoryNarrator,
) : ViewModel() {
    private val storyId = savedStateHandle.toRoute<ReaderRoute>().storyId

    private val _state = MutableStateFlow(ReaderUiState(storyId = storyId))

    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private val effectChannel = Channel<ReaderUiEffect>(Channel.BUFFERED)

    val effects: Flow<ReaderUiEffect> = effectChannel.receiveAsFlow()

    init {
        _state
            .map { LoadKey(it.storyId, it.retryToken) }
            .distinctUntilChanged()
            .flatMapLatest { key -> observeStoryDetail(key.storyId) }
            .onEach { onIntent(ReaderIntent.DetailChanged(it)) }
            .launchIn(viewModelScope)

        observeNarration(storyId)
            .onEach { onIntent(ReaderIntent.NarrationStateChanged(it)) }
            .launchIn(viewModelScope)
    }

    /**
     * The reducer decides what the next state is, which effects the intent raised, and — for the
     * three narration effects — which [StoryNarrator] call they mean; this only carries that
     * decision out. [ReaderEffect.ShowAudioUnavailable] is the one effect meant for the screen, so
     * it is the one forwarded to [effectChannel] instead of acted on here.
     */
    fun onIntent(intent: ReaderIntent) {
        val reduction = _state.value.reduce(intent)
        _state.value = reduction.state
        reduction.effects.forEach { effect ->
            when (effect) {
                is ReaderEffect.StartNarration -> storyNarrator.start(storyId, effect.paragraphs)
                ReaderEffect.PauseNarration -> storyNarrator.pause()
                ReaderEffect.ResumeNarration -> storyNarrator.resume()
                ReaderEffect.ShowAudioUnavailable ->
                    effectChannel.trySend(ReaderUiEffect.ShowAudioUnavailable)
            }
        }
    }

    /**
     * Leaving the screen stops narration and discards its position (FR-007/FR-008) — backgrounding
     * the whole app does not call this, since the composable and this ViewModel stay alive behind
     * the lock screen or another app; only popping the destination off the back stack does.
     */
    override fun onCleared() {
        storyNarrator.stop()
    }
}
