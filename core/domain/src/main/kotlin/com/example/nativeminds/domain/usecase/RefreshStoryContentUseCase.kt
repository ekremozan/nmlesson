package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.repository.StoryRepository
import javax.inject.Inject

/**
 * Pulls a story's text when the device does not have it.
 *
 * The failure is reported *and* returned: reporting alone would leave the reader staring at a
 * spinner, and returning alone would lose the only record that anything went wrong.
 */
class RefreshStoryContentUseCase @Inject constructor(
    private val repository: StoryRepository,
    private val errorReporter: ErrorReporter,
) {
    suspend operator fun invoke(storyId: Long): Result<Unit> =
        runCatching { repository.refreshContent(storyId) }
            .onFailure { errorReporter.report(it, "refreshContent(storyId=$storyId)") }
}
