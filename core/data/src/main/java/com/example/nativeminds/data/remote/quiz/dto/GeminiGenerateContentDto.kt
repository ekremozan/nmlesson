package com.example.nativeminds.data.remote.quiz.dto

import kotlinx.serialization.Serializable

/** Wire shape of the Gemini `generateContent` REST endpoint's request body. */
@Serializable
data class GeminiGenerateContentRequestDto(
    val contents: List<GeminiContentDto>,
    val generationConfig: GeminiGenerationConfigDto,
)

@Serializable
data class GeminiContentDto(val parts: List<GeminiPartDto>)

@Serializable
data class GeminiPartDto(val text: String)

@Serializable
data class GeminiGenerationConfigDto(
    val responseMimeType: String,
    val responseSchema: GeminiSchemaDto,
)

/** A narrow OpenAPI-style subset — only what the quiz question schema needs. */
@Serializable
data class GeminiSchemaDto(
    val type: String,
    val properties: Map<String, GeminiSchemaDto>? = null,
    val items: GeminiSchemaDto? = null,
    val required: List<String>? = null,
)

/** Wire shape of the response body — only the parts the data source reads. */
@Serializable
data class GeminiGenerateContentResponseDto(val candidates: List<GeminiCandidateDto> = emptyList())

@Serializable
data class GeminiCandidateDto(val content: GeminiContentDto? = null)

/**
 * The fixed schema that forces Gemini's reply onto [GeminiQuizPayloadDto]'s exact shape (see
 * `specs/007-ai-quiz-generation/research.md` R2).
 */
val QuizResponseSchema = GeminiSchemaDto(
    type = "OBJECT",
    properties = mapOf(
        "question" to GeminiSchemaDto(type = "STRING"),
        "options" to GeminiSchemaDto(type = "ARRAY", items = GeminiSchemaDto(type = "STRING")),
        "correctOptionIndex" to GeminiSchemaDto(type = "INTEGER"),
        "explanation" to GeminiSchemaDto(type = "STRING"),
    ),
    required = listOf("question", "options", "correctOptionIndex", "explanation"),
)
