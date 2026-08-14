package com.example.nativeminds.domain

import androidx.paging.PagingData
import com.example.nativeminds.domain.repository.StoryRepository
import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Hand-written rather than mocked: the tests here are about a rule, and a fake whose behaviour is
 * visible in twenty lines is easier to trust than a stack of stubbing calls.
 */
class FakeStoryRepository(
    story: Story? = null,
    content: StoryContent? = null,
) : StoryRepository {
    val storyFlow = MutableStateFlow(story)
    val contentFlow = MutableStateFlow(content)

    var refreshCount = 0
        private set

    var refreshFailure: Throwable? = null

    override fun pagedStories(category: String?, query: String): Flow<PagingData<Story>> =
        emptyFlow()

    override fun categories(): Flow<List<String>> = emptyFlow()

    override fun story(id: Long): Flow<Story?> = storyFlow

    override fun storyContent(id: Long): Flow<StoryContent?> = contentFlow

    override suspend fun refreshContent(id: Long) {
        refreshCount++
        refreshFailure?.let { throw it }
    }

    override suspend fun syncIfNeeded() = Unit
}
