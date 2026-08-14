package com.example.nativeminds.feature.reader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.repository.EntitlementRepository
import com.example.nativeminds.domain.repository.StoryRepository
import com.example.nativeminds.domain.usecase.ObserveStoryDetailUseCase
import com.example.nativeminds.domain.usecase.RefreshStoryContentUseCase
import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROBOLECTRIC_SDK = 36

private class TestStoryRepository : StoryRepository {
    val storyFlow = MutableStateFlow<Story?>(unlockedStory)
    val contentFlow = MutableStateFlow<StoryContent?>(storyContent)
    var subscriptions = 0
        private set

    override fun pagedStories(category: String?, query: String): Flow<PagingData<Story>> =
        emptyFlow()

    override fun categories(): Flow<List<String>> = emptyFlow()

    override fun story(id: Long): Flow<Story?> {
        subscriptions++
        return storyFlow
    }

    override fun storyContent(id: Long): Flow<StoryContent?> = contentFlow

    override suspend fun refreshContent(id: Long) = Unit

    override suspend fun syncIfNeeded() = Unit
}

private class TestEntitlementRepository(premium: Boolean = false) : EntitlementRepository {
    val flow = MutableStateFlow(premium)

    override fun isPremium(): Flow<Boolean> = flow
}

private class SilentErrorReporter : ErrorReporter {
    override fun report(throwable: Throwable, context: String) = Unit
}

/**
 * Runs under Robolectric because the ViewModel reads its type-safe route through
 * `SavedStateHandle`, which stores arguments in a real `android.os.Bundle`. The alternative was to
 * read the id by string key in production code, which would have thrown away the reason the route
 * is typed in the first place.
 *
 * Pinned to the newest SDK Robolectric ships a runtime for, which trails the one the module
 * compiles against.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class ReaderViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        repository: StoryRepository,
        entitlements: EntitlementRepository = TestEntitlementRepository(),
    ) = ReaderViewModel(
        savedStateHandle = SavedStateHandle(mapOf("storyId" to unlockedStory.id)),
        observeStoryDetail = ObserveStoryDetailUseCase(
            storyRepository = repository,
            entitlementRepository = entitlements,
            refreshStoryContent = RefreshStoryContentUseCase(repository, SilentErrorReporter()),
        ),
    )

    @Test
    fun contentFromTheDomainLayerReachesStateThroughTheSameDoorAsUserActions() = runTest {
        val repository = TestStoryRepository()

        val state = viewModel(repository).state.first { it.content is ReaderContentUiState.Ready }

        val ready = state.content as ReaderContentUiState.Ready
        assertEquals(unlockedStory.title, ready.story.title)
    }

    @Test
    fun retryResubscribesTheContentStreamExactlyOnce() = runTest {
        val repository = TestStoryRepository()
        val viewModel = viewModel(repository)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }
        val before = repository.subscriptions

        viewModel.onIntent(ReaderIntent.RetryRequested)
        viewModel.state.first { it.retryToken == 1 && it.content is ReaderContentUiState.Ready }

        assertEquals(before + 1, repository.subscriptions)
    }

    @Test
    fun anIntentThatOnlyRaisesAnEffectLeavesStateAlone() = runTest {
        val viewModel = viewModel(TestStoryRepository())
        val before = viewModel.state.first { it.content is ReaderContentUiState.Ready }

        viewModel.onIntent(ReaderIntent.ListenClicked)

        assertEquals(before, viewModel.state.value)
        assertTrue(viewModel.effects.first() == ReaderEffect.ShowAudioUnavailable)
    }

    @Test
    fun aMissingDetailSurfacesAsAnUnavailableScreen() = runTest {
        val repository = TestStoryRepository()
        repository.storyFlow.value = null

        val state = viewModel(repository)
            .state
            .first { it.content is ReaderContentUiState.Unavailable }

        assertTrue(state.content is ReaderContentUiState.Unavailable)
    }

    @Test
    fun loadingIsWhatTheScreenStartsFrom() = runTest {
        val repository = TestStoryRepository()
        repository.contentFlow.value = null

        val state = viewModel(repository).state.first()

        assertEquals(ReaderContentUiState.Loading, state.content)
        assertEquals(ReaderDetail.Loading, ReaderDetail.Loading)
    }
}
