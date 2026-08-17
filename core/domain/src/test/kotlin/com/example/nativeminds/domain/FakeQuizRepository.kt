package com.example.nativeminds.domain

import com.example.nativeminds.domain.model.QuizOption
import com.example.nativeminds.domain.model.QuizQuestion
import com.example.nativeminds.domain.repository.QuizRepository

class FakeQuizRepository(
    private val question: QuizQuestion = QuizQuestion(
        questionText = "What changed?",
        storyTitle = "A Story",
        options = listOf(QuizOption("A", "One"), QuizOption("B", "Two")),
        correctOptionId = "B",
        explanation = "Because.",
    ),
) : QuizRepository {
    var callCount = 0
        private set

    var failure: Throwable? = null

    override suspend fun generateQuestion(storyTitle: String, storyBody: String): QuizQuestion {
        callCount++
        failure?.let { throw it }
        return question
    }
}
