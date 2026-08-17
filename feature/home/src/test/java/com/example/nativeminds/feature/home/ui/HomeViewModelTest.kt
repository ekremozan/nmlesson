package com.example.nativeminds.feature.home.ui

import androidx.paging.PagingData
import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter
import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.domain.usecase.EnsureContentLanguageUseCase
import com.example.nativeminds.domain.usecase.GetPagedLessonsUseCase
import com.example.nativeminds.domain.usecase.GetSubjectsUseCase
import com.example.nativeminds.domain.usecase.SyncLessonsUseCase
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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
    var clearStaleLanguageContentCalls = 0

    /** Complete this from a test to control exactly when the language check resolves. Completed
     * by default so every test that doesn't care about the gate sees it open immediately. */
    var clearStaleLanguageContentGate = CompletableDeferred(Unit)

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

    override suspend fun clearStaleLanguageContent() {
        clearStaleLanguageContentCalls++
        clearStaleLanguageContentGate.await()
    }
}

private class NoOpErrorReporter : ErrorReporter {
    override fun report(throwable: Throwable, context: String) = Unit
}

private class RecordingAnalyticsReporter : AnalyticsReporter {
    val logged = mutableListOf<AnalyticsEvent>()

    override fun log(event: AnalyticsEvent) {
        logged += event
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
    private fun homeViewModel(analyticsReporter: AnalyticsReporter = RecordingAnalyticsReporter()) = HomeViewModel(
        getSubjects = GetSubjectsUseCase(repository),
        getPagedLessons = GetPagedLessonsUseCase(repository),
        syncLessons = SyncLessonsUseCase(repository, NoOpErrorReporter()),
        ensureContentLanguage = EnsureContentLanguageUseCase(repository, NoOpErrorReporter()),
        analyticsReporter = analyticsReporter,
    )

    @Test
    fun `syncs once on creation`() = runTest {
        homeViewModel()

        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `checks the content language once on creation`() = runTest {
        homeViewModel()

        assertEquals(1, repository.clearStaleLanguageContentCalls)
    }

    @Test
    fun `paged lessons are not requested until the content-language check completes`() = runTest {
        repository.clearStaleLanguageContentGate = CompletableDeferred()
        val viewModel = homeViewModel()

        viewModel.collectOnce()

        assertEquals(0, repository.pagingRequests)
    }

    @Test
    fun `paged lessons are requested once the content-language check completes`() = runTest {
        repository.clearStaleLanguageContentGate = CompletableDeferred()
        val viewModel = homeViewModel()
        viewModel.collectOnce()
        assertEquals(0, repository.pagingRequests)

        repository.clearStaleLanguageContentGate.complete(Unit)
        viewModel.collectOnce()

        assertEquals(1, repository.pagingRequests)
    }

    @Test
    fun `a failing sync surfaces an error effect`() = runTest {
        repository.syncFailure = IOException("no network")

        val viewModel = homeViewModel()

        assertEquals(HomeEffect.ShowSyncError, viewModel.effects.first())
    }

    @Test
    fun `refresh requested triggers another sync`() = runTest {
        val viewModel = homeViewModel()
        val syncCallsAfterInit = repository.syncCalls

        viewModel.onIntent(HomeIntent.RefreshRequested)

        assertEquals(syncCallsAfterInit + 1, repository.syncCalls)
        assertEquals(1, viewModel.state.value.syncToken)
    }

    @Test
    fun `a failing refresh surfaces an error effect too`() = runTest {
        val viewModel = homeViewModel()
        repository.syncFailure = IOException("no network")

        viewModel.onIntent(HomeIntent.RefreshRequested)

        assertEquals(HomeEffect.ShowSyncError, viewModel.effects.first())
    }

    @Test
    fun `a persistently failing sync surfaces exactly one error effect per attempt, never a spammed extra`() = runTest {
        repository.syncFailure = IOException("no network")
        val viewModel = homeViewModel()
        val effects = mutableListOf<HomeEffect>()
        val collector = launch { viewModel.effects.toList(effects) }
        runCurrent()

        viewModel.onIntent(HomeIntent.RefreshRequested)
        runCurrent()
        viewModel.onIntent(HomeIntent.RefreshRequested)
        runCurrent()

        collector.cancel()
        assertEquals(listOf(HomeEffect.ShowSyncError, HomeEffect.ShowSyncError, HomeEffect.ShowSyncError), effects)
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

    @Test
    fun `typing a query logs LessonsFiltered only after the debounce settles`() = runTest {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = homeViewModel(analyticsReporter)

        viewModel.onIntent(HomeIntent.QueryChanged("h"))
        viewModel.onIntent(HomeIntent.QueryChanged("hü"))
        viewModel.onIntent(HomeIntent.QueryChanged("hücre"))
        assertTrue(analyticsReporter.logged.isEmpty())

        advanceTimeBy(700)

        assertEquals(listOf(AnalyticsEvent.LessonsFiltered("hücre", null)), analyticsReporter.logged)
    }

    @Test
    fun `selecting a subject logs LessonsFiltered with the subject`() = runTest {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = homeViewModel(analyticsReporter)

        viewModel.onIntent(HomeIntent.SubjectSelected("Kimya"))
        advanceTimeBy(700)

        assertEquals(listOf(AnalyticsEvent.LessonsFiltered(null, "Kimya")), analyticsReporter.logged)
    }

    @Test
    fun `clearing the filter does not log an empty LessonsFiltered`() = runTest {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = homeViewModel(analyticsReporter)
        viewModel.onIntent(HomeIntent.QueryChanged("hücre"))
        advanceTimeBy(700)

        viewModel.onIntent(HomeIntent.QueryCleared)
        advanceTimeBy(700)

        assertEquals(listOf(AnalyticsEvent.LessonsFiltered("hücre", null)), analyticsReporter.logged)
    }
}
