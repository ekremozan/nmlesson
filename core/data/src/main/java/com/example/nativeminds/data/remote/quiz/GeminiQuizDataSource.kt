package com.example.nativeminds.data.remote.quiz

import com.example.nativeminds.data.remote.quiz.dto.GeminiQuizPayloadDto

/** The quiz feature's remote source. [GeminiRemoteQuizDataSource] is the only implementation. */
interface GeminiQuizDataSource {
    /** Throws on network/service failure or when Gemini's reply cannot be parsed. */
    suspend fun generateQuestion(storyTitle: String, storyBody: String): GeminiQuizPayloadDto
}
