package com.example.nativeminds.domain

import androidx.paging.PagingData
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Hand-written rather than mocked: the tests here are about a rule, and a fake whose behaviour is
 * visible in twenty lines is easier to trust than a stack of stubbing calls.
 */
class FakeLessonRepository(
    lesson: Lesson? = null,
    content: LessonContent? = null,
) : LessonRepository {
    val lessonFlow = MutableStateFlow(lesson)
    val contentFlow = MutableStateFlow(content)

    var refreshCount = 0
        private set

    var refreshFailure: Throwable? = null

    var syncCount = 0
        private set

    var syncFailure: Throwable? = null

    override fun pagedLessons(subject: String?, query: String): Flow<PagingData<Lesson>> =
        emptyFlow()

    override fun subjects(): Flow<List<String>> = emptyFlow()

    override fun lesson(id: Long): Flow<Lesson?> = lessonFlow

    override fun lessonContent(id: Long): Flow<LessonContent?> = contentFlow

    override suspend fun refreshContent(id: Long) {
        refreshCount++
        refreshFailure?.let { throw it }
    }

    override suspend fun syncIfNeeded() {
        syncCount++
        syncFailure?.let { throw it }
    }
}
