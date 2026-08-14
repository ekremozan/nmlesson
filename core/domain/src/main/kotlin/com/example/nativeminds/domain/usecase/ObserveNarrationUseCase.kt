package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.forStory
import com.example.nativeminds.domain.narration.StoryNarrator
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The one story's narration state a reader screen may see — the scoping rule ([forStory]) lives
 * here rather than in the ViewModel because "is this narration mine" is a product rule the whole
 * single-narrator-per-app design rests on, not an incidental filter.
 */
class ObserveNarrationUseCase @Inject constructor(
    private val storyNarrator: StoryNarrator,
) {
    operator fun invoke(storyId: Long): Flow<NarrationState> = storyNarrator.state
        .map { it.forStory(storyId) }
        .distinctUntilChanged()
}
