package com.example.nativeminds.data.remote.quiz

import com.example.nativeminds.data.BuildConfig
import com.example.nativeminds.data.remote.quiz.dto.GeminiContentDto
import com.example.nativeminds.data.remote.quiz.dto.GeminiGenerateContentRequestDto
import com.example.nativeminds.data.remote.quiz.dto.GeminiGenerateContentResponseDto
import com.example.nativeminds.data.remote.quiz.dto.GeminiGenerationConfigDto
import com.example.nativeminds.data.remote.quiz.dto.GeminiPartDto
import com.example.nativeminds.data.remote.quiz.dto.GeminiQuizPayloadDto
import com.example.nativeminds.data.remote.quiz.dto.QuizResponseSchema
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.json.Json

private const val GEMINI_MODEL_NAME = "gemini-flash-latest"
private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

/**
 * Calls the Gemini API directly from the device over a plain Ktor client (see
 * `specs/007-ai-quiz-generation/research.md` R1) with `responseSchema` forcing the reply onto
 * [GeminiQuizPayloadDto]'s shape.
 */
class GeminiRemoteQuizDataSource @Inject constructor(
    private val httpClient: HttpClient,
) : GeminiQuizDataSource {
    override suspend fun generateQuestion(storyTitle: String, storyBody: String): GeminiQuizPayloadDto {
        val response = httpClient.post("$GEMINI_BASE_URL/$GEMINI_MODEL_NAME:generateContent") {
            header("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
            contentType(ContentType.Application.Json)
            setBody(
                GeminiGenerateContentRequestDto(
                    contents = listOf(GeminiContentDto(parts = listOf(GeminiPartDto(prompt(storyTitle, storyBody))))),
                    generationConfig = GeminiGenerationConfigDto(
                        responseMimeType = "application/json",
                        responseSchema = QuizResponseSchema,
                    ),
                ),
            )
        }
        if (!response.status.isSuccess()) {
            throw IOException("Gemini request failed with status ${response.status}")
        }

        val json = response.body<GeminiGenerateContentResponseDto>()
            .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IOException("Gemini returned no content")
        return Json.decodeFromString(GeminiQuizPayloadDto.serializer(), json)
    }
}

private fun prompt(storyTitle: String, storyBody: String): String = """
    Hikaye: "$storyTitle"

    $storyBody

    ---
    Bu hikayeye dayalı, tam olarak 4 şıklı ve tek doğru cevaplı bir okuma anlama sorusu üret.
    Doğru cevabın kısa (1-2 cümlelik) bir açıklamasını da ver. Yanıtı yalnızca verilen şemaya
    uygun JSON olarak döndür. Soru ve şıkları Türkçe yaz.
""".trimIndent()
