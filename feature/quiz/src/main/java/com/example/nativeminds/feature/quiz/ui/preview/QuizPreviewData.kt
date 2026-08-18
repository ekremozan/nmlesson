package com.example.nativeminds.feature.quiz.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.feature.quiz.ui.QuizContentUiState
import com.example.nativeminds.feature.quiz.ui.QuizOptionUiModel
import com.example.nativeminds.feature.quiz.ui.QuizOptionVisualState
import com.example.nativeminds.feature.quiz.ui.QuizQuestionUiModel
import com.example.nativeminds.feature.quiz.ui.QuizUiState
import com.example.nativeminds.feature.quiz.ui.mapper.reveal

/** Fixture matching the design's own example question — hand-written so previews need no dependencies. */
val PreviewQuizQuestion = QuizQuestionUiModel(
    questionText = "Bekçinin el yazısı hangi olayla birlikte değişti?",
    storyTitle = "The Lighthouse Keeper's Last Letter",
    options = listOf(
        QuizOptionUiModel("A", "A", "Fenerin elektriğe geçirilmesiyle", QuizOptionVisualState.UNSELECTED),
        QuizOptionUiModel(
            "B",
            "B",
            "Işığın dönüştürülüp saat mekanizmasının indirilmesiyle",
            QuizOptionVisualState.UNSELECTED,
        ),
        QuizOptionUiModel("C", "C", "Yeni bir bekçinin göreve başlamasıyla", QuizOptionVisualState.UNSELECTED),
        QuizOptionUiModel("D", "D", "Fırtınada fenerin hasar görmesiyle", QuizOptionVisualState.UNSELECTED),
    ),
    correctOptionId = "B",
    explanation = "Metne göre el yazısı iki kez değişti; ilki 1931'de ışığın dönüştürülüp saat " +
        "mekanizmasının parça parça aşağı indirildiği yıldı.",
)

/** One quiz state, labelled so a preview row says what it is showing. */
data class QuizPreviewCase(val label: String, val state: QuizUiState)

class QuizPreviewCases : PreviewParameterProvider<QuizPreviewCase> {
    override val values = sequenceOf(
        QuizPreviewCase(
            label = "Unanswered",
            state = QuizUiState(lessonId = 1, content = QuizContentUiState.Ready(PreviewQuizQuestion)),
        ),
        QuizPreviewCase(
            label = "Correct",
            state = QuizUiState(
                lessonId = 1,
                content = QuizContentUiState.Ready(PreviewQuizQuestion.reveal(selectedOptionId = "B")),
            ),
        ),
        QuizPreviewCase(
            label = "Incorrect",
            state = QuizUiState(
                lessonId = 1,
                content = QuizContentUiState.Ready(PreviewQuizQuestion.reveal(selectedOptionId = "A")),
            ),
        ),
        QuizPreviewCase(
            label = "Loading",
            state = QuizUiState(lessonId = 1),
        ),
        QuizPreviewCase(
            label = "Error",
            state = QuizUiState(lessonId = 1, content = QuizContentUiState.Error),
        ),
        QuizPreviewCase(
            label = "Offline",
            state = QuizUiState(lessonId = 1, content = QuizContentUiState.Offline),
        ),
    )
}
