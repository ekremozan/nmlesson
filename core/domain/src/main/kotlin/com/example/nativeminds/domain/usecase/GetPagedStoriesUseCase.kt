package com.example.nativeminds.domain.usecase

import androidx.paging.PagingData
import com.example.nativeminds.domain.repository.StoryRepository
import com.example.nativeminds.model.Story
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * The browse/search read path. Thin today, but it is the seam where premium gating will trim the
 * free user's page — putting that rule here keeps it out of both the ViewModel and the DAO.
 */
class GetPagedStoriesUseCase @Inject constructor(
    private val repository: StoryRepository,
) {
    /** [category] `null` means unfiltered ("All"). */
    operator fun invoke(category: String?, query: String): Flow<PagingData<Story>> =
        repository.pagedStories(category, query)
}
