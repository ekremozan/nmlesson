package com.example.nativeminds.feature.quiz.ui

import com.example.nativeminds.domain.usecase.QuizGenerationResult

/**
 * Everything the quiz screen can be showing. [Locked] is transient — the reducer that reaches it
 * also raises [QuizEffect.NavigateToPaywall], so the screen never has to render a locked layout of
 * its own (see `specs/007-ai-quiz-generation/research.md` R5).
 */
sealed interface QuizContentUiState {
    data object Loading : QuizContentUiState

    data class Ready(val question: QuizQuestionUiModel) : QuizContentUiState

    data object Locked : QuizContentUiState

    data object Error : QuizContentUiState
}

data class QuizUiState(
    val lessonId: Long,
    val content: QuizContentUiState = QuizContentUiState.Loading,
    /** Incremented by a retry so the load key changes and a fresh question is requested. */
    val retryToken: Int = 0,
)

data class QuizQuestionUiModel(
    val questionText: String,
    val storyTitle: String,
    val options: List<QuizOptionUiModel>,
    val correctOptionId: String,
    val explanation: String,
    val selectedOptionId: String? = null,
    val revealed: Boolean = false,
    val isCorrect: Boolean? = null,
)

data class QuizOptionUiModel(
    val id: String,
    val letter: String,
    val text: String,
    val visualState: QuizOptionVisualState,
)

enum class QuizOptionVisualState { UNSELECTED, CORRECT_REVEALED, INCORRECT_REVEALED }

sealed interface QuizIntent {
    /** A question request's outcome, folded back in through the same door as user actions. */
    data class ResultLoaded(val result: QuizGenerationResult) : QuizIntent

    data class OptionSelected(val optionId: String) : QuizIntent

    data object RetryRequested : QuizIntent
}

sealed interface QuizEffect {
    data object NavigateToPaywall : QuizEffect
}

/** The one effect meant for the screen itself. */
sealed interface QuizUiEffect {
    data object NavigateToPaywall : QuizUiEffect
}

/**
 * What a reduction produces: the next state, and any one-shot effects that intent raised — the
 * same shape as `:feature:reader`'s `Reduction` (not yet extracted to a shared module; see the
 * README's Key Decisions).
 */
data class Reduction(
    val state: QuizUiState,
    val effects: List<QuizEffect> = emptyList(),
)
