package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.repository.LessonRepository
import javax.inject.Inject

/**
 * Clears the local lesson cache if it was synced in a different language than the app is showing
 * content in now. A caller awaits this before reading [LessonRepository.pagedLessons], so a screen
 * never renders a lesson in the wrong language while [SyncLessonsUseCase] is still fetching the
 * right one.
 *
 * Never propagates a failure: a caller that awaits this before unblocking its UI would otherwise
 * hang on a rare local-database error, so a failure is reported and swallowed rather than returned.
 */
class EnsureContentLanguageUseCase @Inject constructor(
    private val repository: LessonRepository,
    private val errorReporter: ErrorReporter,
) {
    suspend operator fun invoke() {
        runCatching { repository.clearStaleLanguageContent() }
            .onFailure { errorReporter.report(it, "clearStaleLanguageContent") }
    }
}
