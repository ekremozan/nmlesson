package com.example.nativeminds.domain.model

/**
 * One AI-generated reading-comprehension question for a single lesson, never persisted (see
 * `specs/007-ai-quiz-generation/research.md` R4) — it lives only as long as the screen that asked
 * for it.
 */
data class QuizQuestion(
    val questionText: String,
    val storyTitle: String,
    val options: List<QuizOption>,
    val correctOptionId: String,
    val explanation: String,
)

data class QuizOption(
    val id: String,
    val text: String,
)
