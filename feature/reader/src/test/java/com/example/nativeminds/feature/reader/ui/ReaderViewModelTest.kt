package com.example.nativeminds.feature.reader.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.PagingData
import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.ReaderDetail
import com.example.nativeminds.domain.narration.LessonNarrator
import com.example.nativeminds.domain.observability.AccessLevel
import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter
import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.observability.ListenStopReason
import com.example.nativeminds.domain.repository.EntitlementRepository
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.domain.usecase.ObserveNarrationUseCase
import com.example.nativeminds.domain.usecase.ObserveLessonDetailUseCase
import com.example.nativeminds.domain.usecase.RefreshLessonContentUseCase
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

private class TestLessonRepository : LessonRepository {
    val lessonFlow = MutableStateFlow<Lesson?>(unlockedLesson)
    val contentFlow = MutableStateFlow<LessonContent?>(lessonContent)
    var subscriptions = 0
        private set

    override fun pagedLessons(subject: String?, query: String): Flow<PagingData<Lesson>> =
        emptyFlow()

    override fun subjects(): Flow<List<String>> = emptyFlow()

    override fun lesson(id: Long): Flow<Lesson?> {
        subscriptions++
        return lessonFlow
    }

    override fun lessonContent(id: Long): Flow<LessonContent?> = contentFlow

    override suspend fun refreshContent(id: Long) = Unit

    override suspend fun syncIfNeeded() = Unit
}

private class TestEntitlementRepository(premium: Boolean = false) : EntitlementRepository {
    val flow = MutableStateFlow(premium)

    override fun isPremium(): Flow<Boolean> = flow

    override fun setPremium(value: Boolean) {
        flow.value = value
    }
}

private class SilentErrorReporter : ErrorReporter {
    override fun report(throwable: Throwable, context: String) = Unit
}

private class RecordingAnalyticsReporter : AnalyticsReporter {
    val logged = mutableListOf<AnalyticsEvent>()

    override fun log(event: AnalyticsEvent) {
        logged += event
    }
}

private class TestLessonNarrator : LessonNarrator {
    private val _state = MutableStateFlow<NarrationState>(NarrationState.Idle)
    override val state: StateFlow<NarrationState> = _state.asStateFlow()

    var startedWith: Pair<Long, List<String>>? = null
        private set

    override fun start(lessonId: Long, paragraphs: List<String>) {
        startedWith = lessonId to paragraphs
        _state.value = NarrationState.Playing(lessonId, sentenceIndex = 0, paragraphs.size)
    }

    override fun pause() {
        val current = _state.value
        if (current is NarrationState.Playing) {
            _state.value =
                NarrationState.Paused(current.lessonId, current.sentenceIndex, current.totalSentences)
        }
    }

    override fun resume() {
        val current = _state.value
        if (current is NarrationState.Paused) {
            _state.value =
                NarrationState.Playing(current.lessonId, current.sentenceIndex, current.totalSentences)
        }
    }

    override fun stop() {
        _state.value = NarrationState.Idle
    }

    /** Simulates the engine finishing the lesson on its own, without a `pause()` in between. */
    fun completeNaturally() {
        _state.value = NarrationState.Idle
    }
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
        repository: LessonRepository,
        entitlements: EntitlementRepository = TestEntitlementRepository(),
        narrator: LessonNarrator = TestLessonNarrator(),
        analyticsReporter: AnalyticsReporter = RecordingAnalyticsReporter(),
    ) = ReaderViewModel(
        savedStateHandle = SavedStateHandle(mapOf("lessonId" to unlockedLesson.id)),
        observeLessonDetail = ObserveLessonDetailUseCase(
            lessonRepository = repository,
            entitlementRepository = entitlements,
            refreshLessonContent = RefreshLessonContentUseCase(repository, SilentErrorReporter()),
        ),
        observeNarration = ObserveNarrationUseCase(narrator),
        lessonNarrator = narrator,
        analyticsReporter = analyticsReporter,
    )

    @Test
    fun contentFromTheDomainLayerReachesStateThroughTheSameDoorAsUserActions() = runTest {
        val repository = TestLessonRepository()

        val state = viewModel(repository).state.first { it.content is ReaderContentUiState.Ready }

        val ready = state.content as ReaderContentUiState.Ready
        assertEquals(unlockedLesson.title, ready.lesson.title)
    }

    @Test
    fun retryResubscribesTheContentStreamExactlyOnce() = runTest {
        val repository = TestLessonRepository()
        val viewModel = viewModel(repository)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }
        val before = repository.subscriptions

        viewModel.onIntent(ReaderIntent.RetryRequested)
        viewModel.state.first { it.retryToken == 1 && it.content is ReaderContentUiState.Ready }

        assertEquals(before + 1, repository.subscriptions)
    }

    @Test
    fun listenClickedFromIdleStartsNarrationDirectlyRatherThanThroughTheEffectChannel() = runTest {
        val narrator = TestLessonNarrator()
        val viewModel = viewModel(TestLessonRepository(), narrator = narrator)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }

        viewModel.onIntent(ReaderIntent.ListenClicked)

        assertEquals(unlockedLesson.id to lessonContent.paragraphs, narrator.startedWith)
    }

    @Test
    fun narrationFromTheNarratorFoldsBackIntoState() = runTest {
        val narrator = TestLessonNarrator()
        val viewModel = viewModel(TestLessonRepository(), narrator = narrator)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }

        viewModel.onIntent(ReaderIntent.ListenClicked)
        val playing = viewModel.state.first { it.narration is NarrationState.Playing }

        assertTrue(playing.narration is NarrationState.Playing)
    }

    @Test
    fun clearingTheViewModelStopsNarration() = runTest {
        val narrator = TestLessonNarrator()
        val viewModel = viewModel(TestLessonRepository(), narrator = narrator)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }
        viewModel.onIntent(ReaderIntent.ListenClicked)

        val store = ViewModelStore()
        store.put("reader", viewModel)
        store.clear()

        assertEquals(NarrationState.Idle, narrator.state.value)
    }

    @Test
    fun aMissingDetailSurfacesAsAnUnavailableScreen() = runTest {
        val repository = TestLessonRepository()
        repository.lessonFlow.value = null

        val state = viewModel(repository)
            .state
            .first { it.content is ReaderContentUiState.Unavailable }

        assertTrue(state.content is ReaderContentUiState.Unavailable)
    }

    @Test
    fun loadingIsWhatTheScreenStartsFrom() = runTest {
        val repository = TestLessonRepository()
        repository.contentFlow.value = null

        val state = viewModel(repository).state.first()

        assertEquals(ReaderContentUiState.Loading, state.content)
        assertEquals(ReaderDetail.Loading, ReaderDetail.Loading)
    }

    @Test
    fun contentBecomingReadyLogsContentViewedExactlyOnce() = runTest {
        val repository = TestLessonRepository()
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = viewModel(repository, analyticsReporter = analyticsReporter)

        viewModel.state.first { it.content is ReaderContentUiState.Ready }
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(AnalyticsEvent.ContentViewed(unlockedLesson.id, AccessLevel.FULL)),
            analyticsReporter.logged,
        )
    }

    @Test
    fun listenClickedLogsListenStarted() = runTest {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = viewModel(TestLessonRepository(), analyticsReporter = analyticsReporter)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }

        viewModel.onIntent(ReaderIntent.ListenClicked)

        assertTrue(analyticsReporter.logged.contains(AnalyticsEvent.ListenStarted(unlockedLesson.id)))
    }

    @Test
    fun listenClickedAgainWhilePlayingLogsListenStoppedWithPausedReason() = runTest {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = viewModel(TestLessonRepository(), analyticsReporter = analyticsReporter)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }
        viewModel.onIntent(ReaderIntent.ListenClicked)
        viewModel.state.first { it.narration is NarrationState.Playing }

        viewModel.onIntent(ReaderIntent.ListenClicked)

        val stopped = analyticsReporter.logged.filterIsInstance<AnalyticsEvent.ListenStopped>().single()
        assertEquals(unlockedLesson.id, stopped.lessonId)
        assertEquals(ListenStopReason.PAUSED, stopped.reason)
    }

    @Test
    fun narrationFinishingOnItsOwnLogsListenStoppedWithCompletedReason() = runTest {
        val analyticsReporter = RecordingAnalyticsReporter()
        val narrator = TestLessonNarrator()
        val viewModel = viewModel(TestLessonRepository(), narrator = narrator, analyticsReporter = analyticsReporter)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }
        viewModel.onIntent(ReaderIntent.ListenClicked)
        viewModel.state.first { it.narration is NarrationState.Playing }

        narrator.completeNaturally()
        viewModel.state.first { it.narration is NarrationState.Idle }

        val stopped = analyticsReporter.logged.filterIsInstance<AnalyticsEvent.ListenStopped>().single()
        assertEquals(unlockedLesson.id, stopped.lessonId)
        assertEquals(ListenStopReason.COMPLETED, stopped.reason)
    }

    @Test
    fun pausingThenLeavingTheScreenLogsOnlyOnePausedStop() = runTest {
        val analyticsReporter = RecordingAnalyticsReporter()
        val narrator = TestLessonNarrator()
        val viewModel = viewModel(TestLessonRepository(), narrator = narrator, analyticsReporter = analyticsReporter)
        viewModel.state.first { it.content is ReaderContentUiState.Ready }
        viewModel.onIntent(ReaderIntent.ListenClicked)
        viewModel.state.first { it.narration is NarrationState.Playing }
        viewModel.onIntent(ReaderIntent.ListenClicked)
        viewModel.state.first { it.narration is NarrationState.Paused }

        val store = ViewModelStore()
        store.put("reader", viewModel)
        store.clear()

        assertEquals(
            listOf(ListenStopReason.PAUSED),
            analyticsReporter.logged.filterIsInstance<AnalyticsEvent.ListenStopped>().map { it.reason },
        )
    }
}
