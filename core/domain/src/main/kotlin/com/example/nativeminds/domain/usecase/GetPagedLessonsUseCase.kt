package com.example.nativeminds.domain.usecase

import androidx.paging.PagingData
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.model.Lesson
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * The browse/search read path. Thin today, but it is the seam where premium gating will trim the
 * free user's page — putting that rule here keeps it out of both the ViewModel and the DAO.
 */
class GetPagedLessonsUseCase @Inject constructor(
    private val repository: LessonRepository,
) {
    /** [subject] `null` means unfiltered ("All"). */
    operator fun invoke(subject: String?, query: String): Flow<PagingData<Lesson>> =
        repository.pagedLessons(subject, query)
}
