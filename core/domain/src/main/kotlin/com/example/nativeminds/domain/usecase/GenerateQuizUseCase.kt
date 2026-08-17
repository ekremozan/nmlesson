package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.model.QuizQuestion
import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.repository.EntitlementRepository
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.domain.repository.QuizRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * What asking for a quiz question produced — structurally unable to carry a [QuizQuestion] in the
 * [Locked] case, the same shape [com.example.nativeminds.domain.model.ReaderAccess] uses for the
 * same reason.
 */
sealed interface QuizGenerationResult {
    data class Success(val question: QuizQuestion) : QuizGenerationResult

    data object Locked : QuizGenerationResult

    data class Failed(val throwable: Throwable) : QuizGenerationResult
}

/**
 * Turns a "Test" tap into one fresh AI question for the lesson the reader is on.
 *
 * The entitlement check runs before anything else touches [quizRepository]: a non-premium reader
 * must never cause a Gemini call, not even one whose result is then discarded.
 */
class GenerateQuizUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val entitlementRepository: EntitlementRepository,
    private val quizRepository: QuizRepository,
    private val errorReporter: ErrorReporter,
) {
    suspend operator fun invoke(lessonId: Long): QuizGenerationResult {
        if (!entitlementRepository.isPremium().first()) return QuizGenerationResult.Locked

        val title = lessonRepository.lesson(lessonId).first()?.title
        val paragraphs = lessonRepository.lessonContent(lessonId).first()?.paragraphs

        if (title == null || paragraphs == null) {
            val throwable = IllegalStateException("No stored content for lessonId=$lessonId")
            errorReporter.report(throwable, "generateQuiz(lessonId=$lessonId)")
            return QuizGenerationResult.Failed(throwable)
        }

        return runCatching {
            quizRepository.generateQuestion(title, paragraphs.joinToString(separator = "\n\n"))
        }
            .onFailure { errorReporter.report(it, "generateQuiz(lessonId=$lessonId)") }
            .fold(
                onSuccess = { QuizGenerationResult.Success(it) },
                onFailure = { QuizGenerationResult.Failed(it) },
            )
    }
}
