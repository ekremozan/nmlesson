package com.example.nativeminds.feature.quiz.ui.mapper

import com.example.nativeminds.domain.model.QuizQuestion
import com.example.nativeminds.feature.quiz.ui.QuizOptionUiModel
import com.example.nativeminds.feature.quiz.ui.QuizOptionVisualState
import com.example.nativeminds.feature.quiz.ui.QuizQuestionUiModel

fun QuizQuestion.toUiModel(): QuizQuestionUiModel = QuizQuestionUiModel(
    questionText = questionText,
    storyTitle = storyTitle,
    options = options.map {
        QuizOptionUiModel(
            id = it.id,
            letter = it.id,
            text = it.text,
            visualState = QuizOptionVisualState.UNSELECTED,
        )
    },
    correctOptionId = correctOptionId,
    explanation = explanation,
)

/** Reveals the outcome of picking [selectedOptionId] — the correct option is always marked. */
fun QuizQuestionUiModel.reveal(selectedOptionId: String): QuizQuestionUiModel = copy(
    selectedOptionId = selectedOptionId,
    revealed = true,
    isCorrect = selectedOptionId == correctOptionId,
    options = options.map { option ->
        option.copy(
            visualState = when (option.id) {
                correctOptionId -> QuizOptionVisualState.CORRECT_REVEALED
                selectedOptionId -> QuizOptionVisualState.INCORRECT_REVEALED
                else -> QuizOptionVisualState.UNSELECTED
            },
        )
    },
)
