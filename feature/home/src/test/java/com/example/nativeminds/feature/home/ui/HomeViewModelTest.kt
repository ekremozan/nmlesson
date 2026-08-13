package com.example.nativeminds.feature.home.ui

import androidx.paging.PagingData
import com.example.nativeminds.domain.repository.StoryRepository
import com.example.nativeminds.domain.usecase.GetCategoriesUseCase
import com.example.nativeminds.domain.usecase.GetPagedStoriesUseCase
import com.example.nativeminds.domain.usecase.SyncStoriesUseCase
import com.example.nativeminds.model.Story
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
    var lastCategory: String? = "<not called>"
    var lastQuery: String = "<not called>"

    /** Ordered by story count the way the DAO returns it, so chip order is verifiable. */
    val categories = MutableStateFlow(listOf("Fiction", "Science", "History", "Essays"))

    override fun pagedStories(category: String?, query: String): Flow<PagingData<Story>> {
        lastCategory = category
        lastQuery = query
        return flowOf(PagingData.from(emptyList()))
    }

    override fun categories(): Flow<List<String>> = categories

    override suspend fun syncIfNeeded() {
        syncCalls++
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
     *
     * The collector is not optional — `uiState` is shared with `WhileSubscribed`, so without a
     * subscriber it never leaves its initial value and every assertion below would read an empty
     * chip list.
     */
    private fun TestScope.homeViewModel(): HomeViewModel {
        val viewModel = HomeViewModel(
            getCategories = GetCategoriesUseCase(repository),
            getPagedStories = GetPagedStoriesUseCase(repository),
            syncStories = SyncStoriesUseCase(repository),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `syncs once on creation`() = runTest {
        homeViewModel()

        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `unfiltered state requests no category and empty query`() = runTest {
        val viewModel = homeViewModel()

        viewModel.collectOnce()

        assertNull(repository.lastCategory)
        assertEquals("", repository.lastQuery)
        assertFalse(viewModel.uiState.value.isFiltering)
    }

    @Test
    fun `query change is forwarded trimmed and marks isFiltering`() = runTest {
        val viewModel = homeViewModel()

        viewModel.onQueryChange("  lighthouse  ")
        viewModel.collectOnce()

        assertEquals("lighthouse", repository.lastQuery)
        assertTrue(viewModel.uiState.value.isFiltering)
    }

    @Test
    fun `category selection is forwarded and selects the matching chip`() = runTest {
        val viewModel = homeViewModel()

        viewModel.onCategorySelected("Science")
        viewModel.collectOnce()

        assertEquals("Science", repository.lastCategory)
        assertTrue(viewModel.uiState.value.isFiltering)
        assertTrue(viewModel.uiState.value.chips.first { it.category == "Science" }.isSelected)
    }

    @Test
    fun `chips are the database categories in order, behind an All chip`() = runTest {
        val viewModel = homeViewModel()

        assertEquals(
            listOf(null, "Fiction", "Science", "History", "Essays"),
            viewModel.uiState.value.chips.map { it.category },
        )
        assertTrue(viewModel.uiState.value.chips.first().isSelected)
    }

    @Test
    fun `a new category reaching the database shows up as a chip`() = runTest {
        val viewModel = homeViewModel()

        repository.categories.value = listOf("Fiction", "Science", "History", "Essays", "Poetry")

        assertEquals("Poetry", viewModel.uiState.value.chips.last().category)
    }

    @Test
    fun `picking a suggestion drops the failed query and browses that category`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onQueryChange("quantum lullabies")

        viewModel.onSuggestionSelected("Science")
        viewModel.collectOnce()

        assertEquals("", repository.lastQuery)
        assertEquals("Science", repository.lastCategory)
        assertEquals("", viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.chips.first { it.category == "Science" }.isSelected)
    }

    @Test
    fun `selecting a category from the filter row keeps the query`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onQueryChange("bread")

        viewModel.onCategorySelected("Science")
        viewModel.collectOnce()

        assertEquals("bread", repository.lastQuery)
        assertEquals("Science", repository.lastCategory)
    }

    @Test
    fun `suggestions are the three fullest categories`() = runTest {
        val viewModel = homeViewModel()

        assertEquals(
            listOf("Fiction", "Science", "History"),
            viewModel.uiState.value.suggestions.map { it.category },
        )
    }

    @Test
    fun `clearing resets query and category back to All`() = runTest {
        val viewModel = homeViewModel()
        viewModel.onCategorySelected("Science")
        viewModel.onQueryChange("bread")

        viewModel.onClearQuery()
        viewModel.collectOnce()

        assertNull(repository.lastCategory)
        assertEquals("", repository.lastQuery)
        assertEquals("", viewModel.uiState.value.query)
        assertFalse(viewModel.uiState.value.isFiltering)
        assertTrue(viewModel.uiState.value.chips.first { it.category == null }.isSelected)
    }
}
