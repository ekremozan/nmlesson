package com.example.nativeminds.feature.home.ui

import androidx.paging.PagingData
import com.example.nativeminds.data.StoryRepository
import com.example.nativeminds.model.Story
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
    var lastCategory: String? = "<not called>"
    var lastQuery: String = "<not called>"

    override fun pagedStories(category: String?, query: String): Flow<PagingData<Story>> {
        lastCategory = category
        lastQuery = query
        return flowOf(PagingData.from(emptyList()))
    }

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

    @Test
    fun `syncs once on creation`() = runTest {
        HomeViewModel(repository)

        assertEquals(1, repository.syncCalls)
    }

    @Test
    fun `unfiltered state requests no category and empty query`() = runTest {
        val viewModel = HomeViewModel(repository)

        viewModel.collectOnce()

        assertNull(repository.lastCategory)
        assertEquals("", repository.lastQuery)
        assertFalse(viewModel.uiState.value.isFiltering)
    }

    @Test
    fun `query change is forwarded trimmed and marks isFiltering`() = runTest {
        val viewModel = HomeViewModel(repository)

        viewModel.onQueryChange("  lighthouse  ")
        viewModel.collectOnce()

        assertEquals("lighthouse", repository.lastQuery)
        assertTrue(viewModel.uiState.value.isFiltering)
    }

    @Test
    fun `category selection is forwarded and selects the matching chip`() = runTest {
        val viewModel = HomeViewModel(repository)

        viewModel.onCategorySelected("Science")
        viewModel.collectOnce()

        assertEquals("Science", repository.lastCategory)
        assertTrue(viewModel.uiState.value.isFiltering)
        assertTrue(viewModel.uiState.value.chips.first { it.label == "Science" }.isSelected)
    }

    @Test
    fun `clearing resets query and category back to All`() = runTest {
        val viewModel = HomeViewModel(repository)
        viewModel.onCategorySelected("Science")
        viewModel.onQueryChange("bread")

        viewModel.onClearQuery()
        viewModel.collectOnce()

        assertNull(repository.lastCategory)
        assertEquals("", repository.lastQuery)
        assertEquals("", viewModel.uiState.value.query)
        assertFalse(viewModel.uiState.value.isFiltering)
        assertTrue(viewModel.uiState.value.chips.first { it.label == "All" }.isSelected)
    }
}
