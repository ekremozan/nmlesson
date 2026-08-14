package com.example.nativeminds.feature.reader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.example.nativeminds.designsystem.preview.ScreenThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.reader.R
import com.example.nativeminds.feature.reader.ui.components.ListenPill
import com.example.nativeminds.feature.reader.ui.components.ReaderBody
import com.example.nativeminds.feature.reader.ui.components.ReaderTopBar
import com.example.nativeminds.feature.reader.ui.components.ReaderUnavailableState
import com.example.nativeminds.feature.reader.ui.components.paragraphItemIndex
import com.example.nativeminds.feature.reader.ui.preview.ReaderPreviewCase
import com.example.nativeminds.feature.reader.ui.preview.ReaderPreviewCases
import kotlin.math.ceil
import kotlinx.coroutines.flow.distinctUntilChanged

private const val PERCENT = 100

@Composable
fun ReaderScreen(
    onBack: () -> Unit,
    onUnlockRequested: (storyId: Long, progressPercent: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val audioMessage = stringResource(R.string.reader_audio_unavailable)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.effects.flowWithLifecycle(lifecycle).collect { effect ->
            when (effect) {
                ReaderUiEffect.ShowAudioUnavailable -> snackbarHostState.showSnackbar(audioMessage)
            }
        }
    }

    ReaderScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onUnlockRequested = { onUnlockRequested(state.storyId, state.progressPercent) },
        modifier = modifier,
    )
}

/** Stateless so it is directly usable from previews and Compose tests. */
@Composable
fun ReaderScreenContent(
    state: ReaderUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ReaderIntent) -> Unit,
    onBack: () -> Unit,
    onUnlockRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    ReadingProgressReporter(listState = listState, onIntent = onIntent)
    NarrationScrollFollower(
        listState = listState,
        paragraphIndex = state.narrationHighlight?.paragraphIndex,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReaderTopBar(
                title = state.readyContent?.story?.title.orEmpty(),
                onBack = onBack,
            )

            when (val content = state.content) {
                ReaderContentUiState.Loading -> LoadingBody()

                is ReaderContentUiState.Unavailable -> ReaderUnavailableState(
                    reason = content.reason,
                    onRetry = { onIntent(ReaderIntent.RetryRequested) },
                    modifier = Modifier.padding(top = NativeMindsTheme.spacing.xl),
                )

                is ReaderContentUiState.Ready -> Box(modifier = Modifier.fillMaxSize()) {
                    ReaderBody(
                        story = content.story,
                        body = content.body,
                        listState = listState,
                        contentPadding = PaddingValues(
                            start = NativeMindsTheme.spacing.screen,
                            end = NativeMindsTheme.spacing.screen,
                            bottom = NativeMindsTheme.sizes.footerScrim,
                        ),
                        highlight = state.narrationHighlight,
                    )

                    if (content.body.isTruncated) {
                        BodyFade()
                    }
                }
            }
        }

        ReaderFooter(
            state = state,
            onIntent = onIntent,
            onUnlockRequested = onUnlockRequested,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Turns scrolling into a single intent per whole percent.
 *
 * Progress is measured against what can still be scrolled, not against the item count: a story
 * whose whole text already fits on screen is finished, and one that has not been scrolled at all
 * is at zero however many paragraphs happen to be visible.
 *
 * Without the throttle a drag would push hundreds of state updates a second through the reducer
 * and, from there, through the load key the content flow is derived from.
 */
@Composable
private fun ReadingProgressReporter(listState: LazyListState, onIntent: (ReaderIntent) -> Unit) {
    LaunchedEffect(listState) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val scrollable = layout.totalItemsCount - layout.visibleItemsInfo.size
            if (scrollable <= 0) {
                PERCENT
            } else {
                (listState.firstVisibleItemIndex * PERCENT) / scrollable
            }
        }
            .distinctUntilChanged()
            .collect { onIntent(ReaderIntent.ScrollProgressChanged(it)) }
    }
}

/**
 * Keeps the paragraph being narrated on screen without taking the scroll away from the reader.
 *
 * Keyed on the paragraph rather than the highlight, so it acts once per paragraph instead of once
 * per spoken word, and it stays still whenever the paragraph is already visible or the reader has
 * a drag of their own in flight — an automatic scroll that fights the finger is worse than none.
 */
@Composable
private fun NarrationScrollFollower(listState: LazyListState, paragraphIndex: Int?) {
    LaunchedEffect(listState, paragraphIndex) {
        if (paragraphIndex == null || listState.isScrollInProgress) return@LaunchedEffect
        val item = paragraphItemIndex(paragraphIndex)
        if (listState.layoutInfo.isFullyVisible(item)) return@LaunchedEffect
        listState.animateScrollToItem(item)
    }
}

private fun LazyListLayoutInfo.isFullyVisible(index: Int): Boolean {
    val item = visibleItemsInfo.firstOrNull { it.index == index } ?: return false
    return item.offset >= viewportStartOffset && item.offset + item.size <= viewportEndOffset
}

@Composable
private fun LoadingBody() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * The gradient that dissolves a restricted story into the page.
 *
 * It is decoration over text that was never delivered, not a mask over text that was — the
 * withheld paragraphs are not in the composition at all.
 */
@Composable
private fun BoxScope.BodyFade() {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(NativeMindsTheme.sizes.bodyFade)
            .align(Alignment.BottomCenter)
            .background(
                Brush.verticalGradient(
                    0f to background.copy(alpha = 0f),
                    0.34f to background.copy(alpha = 0.86f),
                    0.62f to background,
                ),
            ),
    )
}

/**
 * What sits above the bottom edge: the listen pill while the story is readable, or the control that
 * brings the unlock sheet back once it has been pushed away.
 */
@Composable
private fun ReaderFooter(
    state: ReaderUiState,
    onIntent: (ReaderIntent) -> Unit,
    onUnlockRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state.readyContent ?: return
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(NativeMindsTheme.sizes.footerScrim)
            .background(
                Brush.verticalGradient(
                    0f to background.copy(alpha = 0f),
                    0.42f to background,
                ),
            )
            .padding(
                horizontal = NativeMindsTheme.spacing.lg,
                vertical = NativeMindsTheme.spacing.screen,
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (state.isRestricted) {
            Text(
                text = stringResource(R.string.reader_unlock_show),
                style = NativeMindsTheme.typography.actionLabel,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NativeMindsTheme.sizes.actionButton)
                    .clip(Pill)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onUnlockRequested)
                    .padding(vertical = NativeMindsTheme.spacing.md),
            )
        } else if (!content.body.isTruncated) {
            ListenPill(
                progress = state.listenPillProgress,
                remainingMinutes = remainingMinutes(
                    totalMinutes = content.story.minutes,
                    progress = state.listenPillProgress,
                ),
                status = state.listenPillStatus,
                onListenClick = { onIntent(ReaderIntent.ListenClicked) },
            )
        }
    }
}

private fun remainingMinutes(totalMinutes: Int, progress: Float): Int =
    ceil(totalMinutes * (1f - progress)).toInt()

/**
 * Every reader state × both themes from one declaration: [ScreenThemePreviews] supplies the
 * light/dark pair on a phone canvas, [ReaderPreviewCases] supplies the states.
 */
@ScreenThemePreviews
@Composable
private fun ReaderScreenContentPreview(
    @PreviewParameter(ReaderPreviewCases::class) case: ReaderPreviewCase,
) {
    NativeMindsTheme {
        ReaderScreenContent(
            state = case.state,
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onBack = {},
            onUnlockRequested = {},
        )
    }
}
