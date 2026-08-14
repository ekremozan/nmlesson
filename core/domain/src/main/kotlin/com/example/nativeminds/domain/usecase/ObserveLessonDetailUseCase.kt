package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.model.ReaderAccess
import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.domain.repository.EntitlementRepository
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformLatest

/**
 * Everything the reader screen needs about one lesson, as a single stream.
 *
 * The gating rule lives here rather than in the ViewModel or the composable: it is the product
 * rule the whole premium proposition rests on, and here it is provable in a plain JVM test and
 * shared by every screen that ever needs it.
 *
 * When the text is not stored yet the stream stays [ReaderDetail.Loading] and a fetch is started;
 * a successful fetch writes to the database, which re-emits through this same combine, so there is
 * one path into the screen rather than a read path and a separate "after refresh" path.
 */
class ObserveLessonDetailUseCase @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val entitlementRepository: EntitlementRepository,
    private val refreshLessonContent: RefreshLessonContentUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(lessonId: Long): Flow<ReaderDetail> = combine(
        lessonRepository.lesson(lessonId),
        lessonRepository.lessonContent(lessonId),
        entitlementRepository.isPremium(),
        ::accessFor,
    ).transformLatest { detail ->
        emit(detail)
        if (detail == ReaderDetail.Loading) {
            refreshLessonContent(lessonId).exceptionOrNull()?.let {
                emit(ReaderDetail.Unavailable(reasonFor(it)))
            }
        }
    }
}

private fun accessFor(
    lesson: Lesson?,
    content: LessonContent?,
    isPremium: Boolean,
): ReaderDetail = when {
    lesson == null -> ReaderDetail.Unavailable(UnavailableReason.LESSON_MISSING)
    content == null -> ReaderDetail.Loading
    !lesson.isLocked || isPremium -> ReaderDetail.Available(ReaderAccess.Full(lesson, content))
    else -> ReaderDetail.Available(
        ReaderAccess.Preview(
            lesson = lesson,
            author = content.author,
            paragraphs = freePreview(content.paragraphs),
            freeSharePercent = FREE_SHARE_PERCENT,
        ),
    )
}

private fun reasonFor(throwable: Throwable): UnavailableReason = when (throwable) {
    is OfflineException -> UnavailableReason.OFFLINE
    else -> UnavailableReason.ERROR
}
