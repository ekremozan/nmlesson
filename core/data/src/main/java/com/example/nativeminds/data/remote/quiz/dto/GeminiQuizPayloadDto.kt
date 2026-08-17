package com.example.nativeminds.data.remote.quiz.dto

import com.example.nativeminds.domain.model.QuizOption
import com.example.nativeminds.domain.model.QuizQuestion
import kotlinx.serialization.Serializable

private val OptionLetters = listOf("A", "B", "C", "D")

/** Gemini's structured-output payload — forced onto this exact shape by the request's `responseSchema`. */
@Serializable
data class GeminiQuizPayloadDto(
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
)

/**
 * Validates the payload against the invariants the domain model relies on rather than trusting
 * the schema alone — a model can still return a technically-valid-JSON payload that violates them
 * (an empty string, an out-of-range index).
 *
 * @throws IllegalStateException when any invariant fails; the caller turns that into a reported,
 * user-visible failure rather than a malformed [QuizQuestion].
 */
fun GeminiQuizPayloadDto.toDomain(storyTitle: String): QuizQuestion {
    check(options.size == OptionLetters.size) {
        "Gemini returned ${options.size} options, expected ${OptionLetters.size}"
    }
    check(correctOptionIndex in options.indices) {
        "correctOptionIndex $correctOptionIndex is out of range for ${options.size} options"
    }
    check(question.isNotBlank()) { "Gemini returned a blank question" }
    check(explanation.isNotBlank()) { "Gemini returned a blank explanation" }

    return QuizQuestion(
        questionText = question,
        storyTitle = storyTitle,
        options = options.mapIndexed { index, text -> QuizOption(id = OptionLetters[index], text = text) },
        correctOptionId = OptionLetters[correctOptionIndex],
        explanation = explanation,
    )
}
