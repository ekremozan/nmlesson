package com.example.nativeminds.feature.quiz.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter
import com.example.nativeminds.domain.repository.EntitlementRepository
import com.example.nativeminds.domain.usecase.GenerateQuizUseCase
import com.example.nativeminds.domain.usecase.QuizGenerationResult
import com.example.nativeminds.feature.quiz.navigation.QuizRoute
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * What the question request is keyed on. A retry changes [retryToken], which changes the key,
 * which re-subscribes — so retry is a pure state transition rather than an imperative reload call.
 * [isPremium] does the same for a purchase completed while this screen is still on the back stack:
 * without it, a key built only from [lessonId] and [retryToken] never changes, so the AI request
 * that a locked result skipped is never retried once the reader becomes premium.
 */
private data class LoadKey(val lessonId: Long, val retryToken: Int, val isPremium: Boolean)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val generateQuiz: GenerateQuizUseCase,
    private val entitlementRepository: EntitlementRepository,
    private val analyticsReporter: AnalyticsReporter,
) : ViewModel() {
    private val lessonId = savedStateHandle.toRoute<QuizRoute>().lessonId

    private val _state = MutableStateFlow(QuizUiState(lessonId = lessonId))

    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private val effectChannel = Channel<QuizUiEffect>(Channel.BUFFERED)

    val effects: Flow<QuizUiEffect> = effectChannel.receiveAsFlow()

    init {
        entitlementRepository.isPremium()
            .onEach { onIntent(QuizIntent.EntitlementChanged(it)) }
            .launchIn(viewModelScope)

        _state
            .map { LoadKey(it.lessonId, it.retryToken, it.isPremium) }
            .distinctUntilChanged()
            .onEach { analyticsReporter.log(AnalyticsEvent.QuizRequested(it.lessonId)) }
            .flatMapLatest { key -> flow { emit(generateQuiz(key.lessonId)) } }
            .onEach { onIntent(QuizIntent.ResultLoaded(it)) }
            .launchIn(viewModelScope)
    }

    /**
     * The reducer decides what the next state is and which effects the intent raised; this only
     * carries that decision out and logs the analytics that follow from it — the same split
     * `ReaderViewModel` uses for narration events.
     */
    fun onIntent(intent: QuizIntent) {
        val reduction = _state.value.reduce(intent)
        _state.value = reduction.state

        if (intent is QuizIntent.ResultLoaded && intent.result is QuizGenerationResult.Success) {
            analyticsReporter.log(AnalyticsEvent.AiFeatureUsed(lessonId, featureName = "quiz"))
        }
        if (intent is QuizIntent.OptionSelected) {
            val question = (reduction.state.content as? QuizContentUiState.Ready)?.question
            question?.isCorrect?.let {
                analyticsReporter.log(AnalyticsEvent.QuizAnswered(lessonId, isCorrect = it))
            }
        }

        reduction.effects.forEach { effect ->
            when (effect) {
                QuizEffect.NavigateToPaywall -> effectChannel.trySend(QuizUiEffect.NavigateToPaywall)
            }
        }
    }
}
