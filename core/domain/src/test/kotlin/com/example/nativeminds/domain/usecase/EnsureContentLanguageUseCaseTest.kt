package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.FakeLessonRepository
import com.example.nativeminds.domain.RecordingErrorReporter
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnsureContentLanguageUseCaseTest {
    private val repository = FakeLessonRepository()
    private val errorReporter = RecordingErrorReporter()
    private val ensureContentLanguage = EnsureContentLanguageUseCase(repository, errorReporter)

    @Test
    fun `a successful check reports nothing`() = runTest {
        ensureContentLanguage()

        assertEquals(1, repository.clearStaleLanguageContentCount)
        assertTrue(errorReporter.reported.isEmpty())
    }

    @Test
    fun `a failed check is reported but not rethrown`() = runTest {
        val failure = IOException("database error")
        repository.clearStaleLanguageContentFailure = failure

        ensureContentLanguage()

        assertEquals(failure, errorReporter.reported.single().first)
    }
}
