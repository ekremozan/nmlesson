package com.example.nativeminds.domain.repository

import com.example.nativeminds.domain.model.QuizQuestion

/**
 * Generates one AI quiz question for a lesson's full text. Implemented in `:core:data` against
 * the Gemini API — see `specs/007-ai-quiz-generation/contracts/gemini-quiz-contract.md`.
 *
 * Throws on network/service/parsing failure rather than returning quietly, matching
 * [com.example.nativeminds.domain.repository.LessonRepository.refreshContent]: the caller is the
 * one that knows how to turn a failure into something the screen can show and the error reporter
 * can record.
 */
interface QuizRepository {
    suspend fun generateQuestion(storyTitle: String, storyBody: String): QuizQuestion
}
