package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.forLesson
import com.example.nativeminds.domain.narration.LessonNarrator
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The one lesson's narration state a reader screen may see — the scoping rule ([forLesson]) lives
 * here rather than in the ViewModel because "is this narration mine" is a product rule the whole
 * single-narrator-per-app design rests on, not an incidental filter.
 */
class ObserveNarrationUseCase @Inject constructor(
    private val lessonNarrator: LessonNarrator,
) {
    operator fun invoke(lessonId: Long): Flow<NarrationState> = lessonNarrator.state
        .map { it.forLesson(lessonId) }
        .distinctUntilChanged()
}
