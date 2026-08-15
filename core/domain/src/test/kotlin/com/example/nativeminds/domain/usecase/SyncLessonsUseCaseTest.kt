package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.FakeLessonRepository
import com.example.nativeminds.domain.RecordingErrorReporter
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncLessonsUseCaseTest {
    private val repository = FakeLessonRepository()
    private val errorReporter = RecordingErrorReporter()
    private val syncLessons = SyncLessonsUseCase(repository, errorReporter)

    @Test
    fun `a successful sync reports nothing`() = runTest {
        val result = syncLessons()

        assertTrue(result.isSuccess)
        assertEquals(1, repository.syncCount)
        assertTrue(errorReporter.reported.isEmpty())
    }

    @Test
    fun `a failed sync is reported and returned as a failure`() = runTest {
        val failure = IOException("no network")
        repository.syncFailure = failure

        val result = syncLessons()

        assertTrue(result.isFailure)
        assertEquals(failure, errorReporter.reported.single().first)
    }
}
