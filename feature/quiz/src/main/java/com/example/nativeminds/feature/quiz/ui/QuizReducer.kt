package com.example.nativeminds.feature.quiz.ui

import com.example.nativeminds.domain.usecase.QuizGenerationResult
import com.example.nativeminds.feature.quiz.ui.mapper.reveal
import com.example.nativeminds.feature.quiz.ui.mapper.toUiModel

/**
 * The only thing in this feature that writes state — pure and top-level, matching
 * `:feature:reader`'s `ReaderReducer`.
 */
fun QuizUiState.reduce(intent: QuizIntent): Reduction = when (intent) {
    is QuizIntent.ResultLoaded -> reduceResultLoaded(intent.result)

    is QuizIntent.OptionSelected -> reduceOptionSelected(intent.optionId)

    QuizIntent.RetryRequested -> Reduction(
        copy(content = QuizContentUiState.Loading, retryToken = retryToken + 1),
    )
}

private fun QuizUiState.reduceResultLoaded(result: QuizGenerationResult): Reduction = when (result) {
    is QuizGenerationResult.Success ->
        Reduction(copy(content = QuizContentUiState.Ready(result.question.toUiModel())))

    QuizGenerationResult.Locked ->
        Reduction(copy(content = QuizContentUiState.Locked), listOf(QuizEffect.NavigateToPaywall))

    is QuizGenerationResult.Failed -> Reduction(copy(content = QuizContentUiState.Error))
}

private fun QuizUiState.reduceOptionSelected(optionId: String): Reduction {
    val ready = content as? QuizContentUiState.Ready ?: return Reduction(this)
    if (ready.question.revealed) return Reduction(this)
    return Reduction(copy(content = ready.copy(question = ready.question.reveal(optionId))))
}
