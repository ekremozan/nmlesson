package com.example.nativeminds.data.remote.quiz.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

private val validPayload = GeminiQuizPayloadDto(
    question = "What changed the keeper's handwriting?",
    options = listOf("Weather", "The light conversion", "A new keeper", "Storm damage"),
    correctOptionIndex = 1,
    explanation = "The log records the 1931 conversion.",
)

class GeminiQuizPayloadDtoMapperTest {
    @Test
    fun aValidPayloadMapsToADomainQuestion() {
        val question = validPayload.toDomain(storyTitle = "The Lighthouse Keeper")

        assertEquals("The Lighthouse Keeper", question.storyTitle)
        assertEquals(validPayload.question, question.questionText)
        assertEquals(listOf("A", "B", "C", "D"), question.options.map { it.id })
        assertEquals("B", question.correctOptionId)
        assertEquals(validPayload.explanation, question.explanation)
    }

    @Test
    fun aPayloadWithoutFourOptionsFailsToMap() {
        val payload = validPayload.copy(options = listOf("Weather", "The light conversion"))

        assertThrows(IllegalStateException::class.java) { payload.toDomain("Title") }
    }

    @Test
    fun anOutOfRangeCorrectIndexFailsToMap() {
        val payload = validPayload.copy(correctOptionIndex = 4)

        assertThrows(IllegalStateException::class.java) { payload.toDomain("Title") }
    }

    @Test
    fun aBlankQuestionFailsToMap() {
        val payload = validPayload.copy(question = "   ")

        assertThrows(IllegalStateException::class.java) { payload.toDomain("Title") }
    }

    @Test
    fun aBlankExplanationFailsToMap() {
        val payload = validPayload.copy(explanation = "")

        assertThrows(IllegalStateException::class.java) { payload.toDomain("Title") }
    }
}
