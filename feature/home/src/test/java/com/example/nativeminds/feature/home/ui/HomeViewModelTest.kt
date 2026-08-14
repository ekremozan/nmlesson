package com.example.nativeminds.feature.home.ui

import androidx.paging.PagingData
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.domain.usecase.GetPagedLessonsUseCase
import com.example.nativeminds.domain.usecase.GetSubjectsUseCase
import com.example.nativeminds.domain.usecase.SyncLessonsUseCase
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Records the parameters it was last asked for — the filtering logic itself now lives in the DAO
 * query, tested by `core:database`'s `LessonDaoTest` instead. */
private class RecordingLessonRepository : LessonRepository {
    var syncCalls = 0
    var syncFailure: Throwable? = null
    var lastSubject: String? = "<not called>"
    var lastQuery: String = "<not called>"
    var pagingRequests = 0

    /** Ordered by lesson count the way the DAO returns it, so chip order is verifiable. */
    val subjects = MutableStateFlow(listOf("Biyoloji", "Kimya", "Tarih", "Coğrafya"))

    override fun pagedLessons(subject: String?, query: String): Flow<PagingData<Lesson>> {
        pagingRequests++
        lastSubject = subject
        lastQuery = query
        return flowOf(PagingData.from(emptyList()))
    }

    override fun subjects(): Flow<List<String>> = subjects

    override fun lesson(id: Long): Flow<Lesson?> = flowOf(null)

    override fun lessonContent(id: Long): Flow<LessonContent?> = flowOf(null)

    override suspend fun refreshContent(id: Long) = Unit

    override suspend fun syncIfNeeded() {
        syncCalls++
        syncFailure?.let { throw it }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val repository = RecordingLessonRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun HomeViewModel.collectOnce() {
        pagedLessons.take(1).collect {}
    }

    /**
     * Hilt is deliberately not involved: the ViewModel takes its collaborators through the
     * constructor, so the test builds the use cases over a fake repository by hand and stays a
     * plain JVM test.
     */
    private fun homeViewModel() = HomeViewModel(
        getSubjects = GetSubjectsUseCase(repository),
        getPagedLessons = GetPagedLessonsUseCase(repository),
        syncLessons = SyncLessonsUseCase(repository),
    )

    @Test
    fun `syncs once on creation`() = runTest {
        homeViewModel()

        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `a failing sync surfaces an error effect`() = runTest {
        repository.syncFailure = IOException("no network")

        val viewModel = homeViewModel()

        assertEquals(HomeEffect.ShowSyncError, viewModel.effects.first())
    }

    @Test
    fun `unfiltered state requests no subject and empty query`() = runTest {
        val viewModel = homeViewModel()

        viewModel.collectOnce()

        assertNull(repository.lastSubject)
        assertEquals("", repository.lastQuery)
        assertFalse(viewModel.state.value.isFiltering)
    }

    @Test
    fun `query change is forwarded trimmed and marks isFiltering`() = runTest {
        val viewModel = homeViewModel()

        viewModel.onIntent(HomeIntent.QueryChanged("  hücre  "))
        viewModel.collectOnce()

        assertEquals("hücre", repository.lastQuery)
        assertTrue(viewModel.state.value.isFiltering)
    }

    @Test
    fun `subject selection is forwarded and selects the matching chip`() = runTest {
        val viewModel = homeViewModel()

        viewModel.onIntent(HomeIntent.SubjectSelected("Kimya"))
        viewModel.collectOnce()

        assertEquals("Kimya", repository.lastSubject)
        assertTrue(viewModel.state.value.isFiltering)
        assertTrue(viewModel.state.value.chips.first { it.subject == "Kimya" }.isSelected)
    }

    @Test
    fun `chips are the database subjects in order, behind an All chip`() = runTest {
        val viewModel = homeViewModel()

        assertEquals(
            listOf(null, "Biyoloji", "Kimya", "Tarih", "Coğrafya"),
            viewModel.state.value.chips.map { it.subject },
        )
        assertTrue(viewModel.state.value.chips.first().isSelected)
    }

    @Test
    fun `a new subject reaching the database shows up as a chip`() = runTest {
        val viewModel = homeViewModel()

        repository.subjects.value = listOf("Biyoloji", "Kimya", "Tarih", "Coğrafya", "Fizik")

        assertEquals("Fizik", viewModel.state.value.chips.last().subject)
    }

    @Test
    fun `a subject list update does not re-request the pager`() = runTest {
        val viewModel = homeViewModel()
        viewModel.collectOnce()
        val requestsBefore = repository.pagingRequests

        repository.subjects.value = listOf("Biyoloji", "Kimya", "Tarih", "Coğrafya", "Fizik")
        viewModel.collectOnce()

        assertEquals(requestsBefore, repository.pagingRequests)
    }

    @Test
    fun `picking a suggestion drops the failed query and browses that subject`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onIntent(HomeIntent.QueryChanged("quantum lullabies"))

        viewModel.onIntent(HomeIntent.SuggestionSelected("Kimya"))
        viewModel.collectOnce()

        assertEquals("", repository.lastQuery)
        assertEquals("Kimya", repository.lastSubject)
        assertEquals("", viewModel.state.value.query)
        assertTrue(viewModel.state.value.chips.first { it.subject == "Kimya" }.isSelected)
    }

    @Test
    fun `selecting a subject from the filter row keeps the query`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onIntent(HomeIntent.QueryChanged("hücre"))

        viewModel.onIntent(HomeIntent.SubjectSelected("Kimya"))
        viewModel.collectOnce()

        assertEquals("hücre", repository.lastQuery)
        assertEquals("Kimya", repository.lastSubject)
    }

    @Test
    fun `suggestions are the three fullest subjects`() = runTest {
        val viewModel = homeViewModel()

        assertEquals(
            listOf("Biyoloji", "Kimya", "Tarih"),
            viewModel.state.value.suggestions.map { it.subject },
        )
    }

    @Test
    fun `clearing resets query and subject back to All`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onIntent(HomeIntent.SubjectSelected("Kimya"))
        viewModel.onIntent(HomeIntent.QueryChanged("hücre"))

        viewModel.onIntent(HomeIntent.QueryCleared)
        viewModel.collectOnce()

        assertNull(repository.lastSubject)
        assertEquals("", repository.lastQuery)
        assertEquals("", viewModel.state.value.query)
        assertFalse(viewModel.state.value.isFiltering)
        assertTrue(viewModel.state.value.chips.first { it.subject == null }.isSelected)
    }
}
