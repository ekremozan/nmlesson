package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.FakeEntitlementRepository
import com.example.nativeminds.domain.FakeLessonRepository
import com.example.nativeminds.domain.FakeQuizRepository
import com.example.nativeminds.domain.RecordingErrorReporter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateQuizUseCaseTest {
    private val entitlements = FakeEntitlementRepository()
    private val errorReporter = RecordingErrorReporter()

    private fun useCase(
        lessonRepository: FakeLessonRepository,
        quizRepository: FakeQuizRepository,
    ) = GenerateQuizUseCase(lessonRepository, entitlements, quizRepository, errorReporter)

    @Test
    fun aNonPremiumReaderNeverCausesAQuizRepositoryCall() = runTest {
        val lessons = FakeLessonRepository(unlockedLesson, lessonContent)
        val quizzes = FakeQuizRepository()

        val result = useCase(lessons, quizzes).invoke(unlockedLesson.id)

        assertEquals(QuizGenerationResult.Locked, result)
        assertEquals(0, quizzes.callCount)
    }

    @Test
    fun aPremiumReaderReceivesTheGeneratedQuestion() = runTest {
        entitlements.setPremium(true)
        val lessons = FakeLessonRepository(unlockedLesson, lessonContent)
        val quizzes = FakeQuizRepository()

        val result = useCase(lessons, quizzes).invoke(unlockedLesson.id)

        assertTrue(result is QuizGenerationResult.Success)
        assertEquals(1, quizzes.callCount)
    }

    @Test
    fun aRepositoryFailureIsReportedAndReturnedAsFailed() = runTest {
        entitlements.setPremium(true)
        val lessons = FakeLessonRepository(unlockedLesson, lessonContent)
        val quizzes = FakeQuizRepository()
        quizzes.failure = IllegalStateException("boom")

        val result = useCase(lessons, quizzes).invoke(unlockedLesson.id)

        assertTrue(result is QuizGenerationResult.Failed)
        assertEquals(1, errorReporter.reported.size)
    }

    @Test
    fun missingLocalContentFailsWithoutCallingTheQuizRepository() = runTest {
        entitlements.setPremium(true)
        val lessons = FakeLessonRepository(unlockedLesson, content = null)
        val quizzes = FakeQuizRepository()

        val result = useCase(lessons, quizzes).invoke(unlockedLesson.id)

        assertTrue(result is QuizGenerationResult.Failed)
        assertEquals(0, quizzes.callCount)
        assertEquals(1, errorReporter.reported.size)
    }
}
