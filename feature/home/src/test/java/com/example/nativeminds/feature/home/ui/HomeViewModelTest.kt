package com.example.nativeminds.feature.home.ui

import androidx.paging.PagingData
import com.example.nativeminds.domain.repository.StoryRepository
import com.example.nativeminds.domain.usecase.GetCategoriesUseCase
import com.example.nativeminds.domain.usecase.GetPagedStoriesUseCase
import com.example.nativeminds.domain.usecase.SyncStoriesUseCase
import com.example.nativeminds.model.Story
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
 * query, tested by `core:database`'s `StoryDaoTest` instead. */
private class RecordingStoryRepository : StoryRepository {
    var syncCalls = 0
    var syncFailure: Throwable? = null
    var lastCategory: String? = "<not called>"
    var lastQuery: String = "<not called>"
    var pagingRequests = 0

    /** Ordered by story count the way the DAO returns it, so chip order is verifiable. */
    val categories = MutableStateFlow(listOf("Fiction", "Science", "History", "Essays"))

    override fun pagedStories(category: String?, query: String): Flow<PagingData<Story>> {
        pagingRequests++
        lastCategory = category
        lastQuery = query
        return flowOf(PagingData.from(emptyList()))
    }

    override fun categories(): Flow<List<String>> = categories

    override suspend fun syncIfNeeded() {
        syncCalls++
        syncFailure?.let { throw it }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val repository = RecordingStoryRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun HomeViewModel.collectOnce() {
        pagedStories.take(1).collect {}
    }

    /**
     * Hilt is deliberately not involved: the ViewModel takes its collaborators through the
     * constructor, so the test builds the use cases over a fake repository by hand and stays a
     * plain JVM test.
     */
    private fun homeViewModel() = HomeViewModel(
        getCategories = GetCategoriesUseCase(repository),
        getPagedStories = GetPagedStoriesUseCase(repository),
        syncStories = SyncStoriesUseCase(repository),
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
    fun `unfiltered state requests no category and empty query`() = runTest {
        val viewModel = homeViewModel()

        viewModel.collectOnce()

        assertNull(repository.lastCategory)
        assertEquals("", repository.lastQuery)
        assertFalse(viewModel.state.value.isFiltering)
    }

    @Test
    fun `query change is forwarded trimmed and marks isFiltering`() = runTest {
        val viewModel = homeViewModel()

        viewModel.onIntent(HomeIntent.QueryChanged("  lighthouse  "))
        viewModel.collectOnce()

        assertEquals("lighthouse", repository.lastQuery)
        assertTrue(viewModel.state.value.isFiltering)
    }

    @Test
    fun `category selection is forwarded and selects the matching chip`() = runTest {
        val viewModel = homeViewModel()

        viewModel.onIntent(HomeIntent.CategorySelected("Science"))
        viewModel.collectOnce()

        assertEquals("Science", repository.lastCategory)
        assertTrue(viewModel.state.value.isFiltering)
        assertTrue(viewModel.state.value.chips.first { it.category == "Science" }.isSelected)
    }

    @Test
    fun `chips are the database categories in order, behind an All chip`() = runTest {
        val viewModel = homeViewModel()

        assertEquals(
            listOf(null, "Fiction", "Science", "History", "Essays"),
            viewModel.state.value.chips.map { it.category },
        )
        assertTrue(viewModel.state.value.chips.first().isSelected)
    }

    @Test
    fun `a new category reaching the database shows up as a chip`() = runTest {
        val viewModel = homeViewModel()

        repository.categories.value = listOf("Fiction", "Science", "History", "Essays", "Poetry")

        assertEquals("Poetry", viewModel.state.value.chips.last().category)
    }

    @Test
    fun `a category list update does not re-request the pager`() = runTest {
        val viewModel = homeViewModel()
        viewModel.collectOnce()
        val requestsBefore = repository.pagingRequests

        repository.categories.value = listOf("Fiction", "Science", "History", "Essays", "Poetry")
        viewModel.collectOnce()

        assertEquals(requestsBefore, repository.pagingRequests)
    }

    @Test
    fun `picking a suggestion drops the failed query and browses that category`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onIntent(HomeIntent.QueryChanged("quantum lullabies"))

        viewModel.onIntent(HomeIntent.SuggestionSelected("Science"))
        viewModel.collectOnce()

        assertEquals("", repository.lastQuery)
        assertEquals("Science", repository.lastCategory)
        assertEquals("", viewModel.state.value.query)
        assertTrue(viewModel.state.value.chips.first { it.category == "Science" }.isSelected)
    }

    @Test
    fun `selecting a category from the filter row keeps the query`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onIntent(HomeIntent.QueryChanged("bread"))

        viewModel.onIntent(HomeIntent.CategorySelected("Science"))
        viewModel.collectOnce()

        assertEquals("bread", repository.lastQuery)
        assertEquals("Science", repository.lastCategory)
    }

    @Test
    fun `suggestions are the three fullest categories`() = runTest {
        val viewModel = homeViewModel()

        assertEquals(
            listOf("Fiction", "Science", "History"),
            viewModel.state.value.suggestions.map { it.category },
        )
    }

    @Test
    fun `clearing resets query and category back to All`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onIntent(HomeIntent.CategorySelected("Science"))
        viewModel.onIntent(HomeIntent.QueryChanged("bread"))

        viewModel.onIntent(HomeIntent.QueryCleared)
        viewModel.collectOnce()

        assertNull(repository.lastCategory)
        assertEquals("", repository.lastQuery)
        assertEquals("", viewModel.state.value.query)
        assertFalse(viewModel.state.value.isFiltering)
        assertTrue(viewModel.state.value.chips.first { it.category == null }.isSelected)
    }
}
