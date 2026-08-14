package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.repository.LessonRepository
import javax.inject.Inject

/**
 * Pulls a lesson's text when the device does not have it.
 *
 * The failure is reported *and* returned: reporting alone would leave the reader staring at a
 * spinner, and returning alone would lose the only record that anything went wrong.
 */
class RefreshLessonContentUseCase @Inject constructor(
    private val repository: LessonRepository,
    private val errorReporter: ErrorReporter,
) {
    suspend operator fun invoke(lessonId: Long): Result<Unit> =
        runCatching { repository.refreshContent(lessonId) }
            .onFailure { errorReporter.report(it, "refreshContent(lessonId=$lessonId)") }
}
