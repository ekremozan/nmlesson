package com.example.nativeminds.feature.quiz.ui

import com.example.nativeminds.domain.model.QuizOption
import com.example.nativeminds.domain.model.QuizQuestion
import com.example.nativeminds.domain.usecase.QuizGenerationResult
import com.example.nativeminds.feature.quiz.ui.mapper.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private val question = QuizQuestion(
    questionText = "What changed?",
    storyTitle = "A Story",
    options = listOf(QuizOption("A", "One"), QuizOption("B", "Two")),
    correctOptionId = "B",
    explanation = "Because.",
)

class QuizReducerTest {
    @Test
    fun aSuccessfulLoadFillsTheScreenWithNoEffects() {
        val reduction = QuizUiState(lessonId = 1)
            .reduce(QuizIntent.ResultLoaded(QuizGenerationResult.Success(question)))

        val content = reduction.state.content as QuizContentUiState.Ready
        assertEquals(question.questionText, content.question.questionText)
        assertFalseRevealed(content)
        assertTrue(reduction.effects.isEmpty())
    }

    @Test
    fun aLockedResultRaisesANavigateToPaywallEffect() {
        val reduction = QuizUiState(lessonId = 1)
            .reduce(QuizIntent.ResultLoaded(QuizGenerationResult.Locked))

        assertEquals(QuizContentUiState.Locked, reduction.state.content)
        assertEquals(listOf(QuizEffect.NavigateToPaywall), reduction.effects)
    }

    @Test
    fun aFailedResultShowsTheErrorState() {
        val reduction = QuizUiState(lessonId = 1)
            .reduce(QuizIntent.ResultLoaded(QuizGenerationResult.Failed(IllegalStateException())))

        assertEquals(QuizContentUiState.Error, reduction.state.content)
    }

    @Test
    fun anOfflineResultShowsTheOfflineState() {
        val reduction = QuizUiState(lessonId = 1)
            .reduce(QuizIntent.ResultLoaded(QuizGenerationResult.Offline))

        assertEquals(QuizContentUiState.Offline, reduction.state.content)
        assertTrue(reduction.effects.isEmpty())
    }

    @Test
    fun selectingTheCorrectOptionRevealsItAsCorrect() {
        val ready = QuizUiState(lessonId = 1, content = QuizContentUiState.Ready(question.toReadyUiModel()))

        val reduction = ready.reduce(QuizIntent.OptionSelected("B"))

        val content = reduction.state.content as QuizContentUiState.Ready
        assertTrue(content.question.revealed)
        assertEquals(true, content.question.isCorrect)
    }

    @Test
    fun selectingAWrongOptionRevealsItAsIncorrect() {
        val ready = QuizUiState(lessonId = 1, content = QuizContentUiState.Ready(question.toReadyUiModel()))

        val reduction = ready.reduce(QuizIntent.OptionSelected("A"))

        val content = reduction.state.content as QuizContentUiState.Ready
        assertTrue(content.question.revealed)
        assertEquals(false, content.question.isCorrect)
    }

    @Test
    fun selectingAnOptionAfterRevealIsIgnored() {
        val revealed = QuizUiState(lessonId = 1, content = QuizContentUiState.Ready(question.toReadyUiModel()))
            .reduce(QuizIntent.OptionSelected("A")).state

        val reduction = revealed.reduce(QuizIntent.OptionSelected("B"))

        assertEquals(revealed, reduction.state)
    }

    @Test
    fun retryMovesBackToLoadingAndIncrementsTheToken() {
        val errored = QuizUiState(lessonId = 1, content = QuizContentUiState.Error)

        val reduction = errored.reduce(QuizIntent.RetryRequested)

        assertEquals(QuizContentUiState.Loading, reduction.state.content)
        assertEquals(1, reduction.state.retryToken)
    }

    @Test
    fun becomingPremiumWhileLockedMovesBackToLoading() {
        val locked = QuizUiState(lessonId = 1, content = QuizContentUiState.Locked, isPremium = false)

        val reduction = locked.reduce(QuizIntent.EntitlementChanged(isPremium = true))

        assertEquals(QuizContentUiState.Loading, reduction.state.content)
        assertTrue(reduction.state.isPremium)
    }

    @Test
    fun anUnchangedEntitlementLeavesContentAlone() {
        val ready = QuizUiState(
            lessonId = 1,
            content = QuizContentUiState.Ready(question.toReadyUiModel()),
            isPremium = true,
        )

        val reduction = ready.reduce(QuizIntent.EntitlementChanged(isPremium = true))

        assertEquals(ready, reduction.state)
    }
}

private fun assertFalseRevealed(content: QuizContentUiState.Ready) {
    assertEquals(false, content.question.revealed)
}

private fun QuizQuestion.toReadyUiModel() = toUiModel()
