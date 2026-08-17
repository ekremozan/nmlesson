package com.example.nativeminds.feature.quiz.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.nativeminds.feature.quiz.ui.QuizScreen
import kotlinx.serialization.Serializable

/** The lesson id is the destination's only argument — the quiz is always about one lesson's text. */
@Serializable
data class QuizRoute(val lessonId: Long)

fun NavGraphBuilder.quizScreen(
    onBack: () -> Unit,
    onPaywallRequested: (lessonId: Long) -> Unit,
) {
    composable<QuizRoute> {
        QuizScreen(onBack = onBack, onPaywallRequested = onPaywallRequested)
    }
}
