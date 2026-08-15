package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.repository.LessonRepository
import javax.inject.Inject

/**
 * Refreshes the local catalog from the remote content source when online; a no-op while offline.
 *
 * The failure is reported *and* returned, matching [RefreshLessonContentUseCase]: reporting alone
 * would leave the caller with nothing to show for a failed sync, and returning alone would lose the
 * only record that anything went wrong.
 */
class SyncLessonsUseCase @Inject constructor(
    private val repository: LessonRepository,
    private val errorReporter: ErrorReporter,
) {
    suspend operator fun invoke(): Result<Unit> =
        runCatching { repository.syncIfNeeded() }
            .onFailure { errorReporter.report(it, "syncIfNeeded") }
}
