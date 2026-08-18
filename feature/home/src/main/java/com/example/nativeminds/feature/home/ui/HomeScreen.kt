package com.example.nativeminds.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ScreenThemePreviews
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.feature.home.R
import com.example.nativeminds.feature.home.ui.components.EmptyResultsState
import com.example.nativeminds.feature.home.ui.components.SearchField
import com.example.nativeminds.feature.home.ui.components.LessonCard
import com.example.nativeminds.feature.home.ui.components.SubjectChipRow
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.LessonUiModel
import com.example.nativeminds.feature.home.ui.preview.HomePreviewCase
import com.example.nativeminds.feature.home.ui.preview.HomePreviewCases
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeScreen(
    onLessonClick: (lessonId: Long, title: String, index: Int) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lessons = viewModel.pagedLessons.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    val syncErrorMessage = stringResource(R.string.home_sync_error)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        viewModel.effects.flowWithLifecycle(lifecycle).collect { effect ->
            when (effect) {
                HomeEffect.ShowSyncError -> snackbarHostState.showSnackbar(syncErrorMessage)
            }
        }
    }

    HomeScreenContent(
        state = state,
        lessons = lessons,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onLessonClick = onLessonClick,
        onProfileClick = onProfileClick,
        modifier = modifier,
    )
}

/** Stateless so it's directly usable from `@Preview`s and future tests. */
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    lessons: LazyPagingItems<LessonUiModel>,
    snackbarHostState: SnackbarHostState,
    onIntent: (HomeIntent) -> Unit,
    onLessonClick: (lessonId: Long, title: String, index: Int) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEmpty = lessons.itemCount == 0 && lessons.loadState.refresh is LoadState.NotLoading

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PullToRefreshBox(
            isRefreshing = lessons.loadState.refresh is LoadState.Loading,
            onRefresh = { onIntent(HomeIntent.RefreshRequested) },
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.padding(horizontal = NativeMindsTheme.spacing.screen),
                    verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.lg),
                ) {
                    GreetingHeader(
                        greeting = state.greeting,
                        userName = state.userName,
                        onProfileClick = onProfileClick,
                    )
                    SearchField(
                        query = state.query,
                        onQueryChange = { onIntent(HomeIntent.QueryChanged(it)) },
                        onClear = { onIntent(HomeIntent.QueryCleared) },
                    )
                }

                SubjectChipRow(
                    chips = state.chips,
                    onChipSelected = { onIntent(HomeIntent.SubjectSelected(it)) },
                    modifier = Modifier.padding(
                        top = NativeMindsTheme.spacing.lg,
                        start = NativeMindsTheme.spacing.screen,
                        end = NativeMindsTheme.spacing.screen,
                    ),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = NativeMindsTheme.spacing.screen,
                            end = NativeMindsTheme.spacing.screen,
                            top = NativeMindsTheme.spacing.xl,
                            bottom = NativeMindsTheme.spacing.md,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (state.isFiltering) {
                            stringResource(R.string.home_heading_results)
                        } else {
                            stringResource(R.string.home_heading_for_you)
                        }.uppercase(),
                        style = NativeMindsTheme.typography.sectionHeading,
                        color = NativeMindsTheme.colors.textMuted,
                    )
                    Text(
                        text = pluralStringResource(R.plurals.home_lesson_count, lessons.itemCount, lessons.itemCount),
                        style = NativeMindsTheme.typography.sectionCount,
                        color = NativeMindsTheme.colors.textSubtle,
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = NativeMindsTheme.spacing.screen),
                ) {
                    if (isEmpty) {
                        item {
                            EmptyResultsState(
                                isFiltering = state.isFiltering,
                                query = state.query,
                                suggestions = state.suggestions,
                                onSuggestionSelected = { onIntent(HomeIntent.SuggestionSelected(it)) },
                                onClearSearch = { onIntent(HomeIntent.QueryCleared) },
                            )
                        }
                    } else {
                        items(count = lessons.itemCount, key = lessons.itemKey { it.id }) { index ->
                            val lesson = lessons[index] ?: return@items
                            LessonCard(
                                lesson = lesson,
                                onClick = { onLessonClick(lesson.id, lesson.title, index) },
                                modifier = Modifier.padding(
                                    horizontal = NativeMindsTheme.spacing.screen,
                                    vertical = 7.dp,
                                ),
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = NativeMindsTheme.spacing.screen),
        )
    }
}

@Composable
private fun GreetingHeader(greeting: GreetingPeriod, userName: String, onProfileClick: () -> Unit) {
    val profileContentDescription = stringResource(R.string.home_avatar_cd)
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                text = stringResource(greeting.toStringRes()),
                style = NativeMindsTheme.typography.greetingLabel,
                color = NativeMindsTheme.colors.textMuted,
            )
            Text(
                text = userName,
                style = NativeMindsTheme.typography.greetingName,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, NativeMindsTheme.colors.cardBorder, CircleShape)
                .clickable(onClick = onProfileClick)
                .semantics { contentDescription = profileContentDescription },
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Person(tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
        }
    }
}

private fun GreetingPeriod.toStringRes() = when (this) {
    GreetingPeriod.MORNING -> R.string.home_greeting_morning
    GreetingPeriod.AFTERNOON -> R.string.home_greeting_afternoon
    GreetingPeriod.EVENING -> R.string.home_greeting_evening
}

/**
 * Every content state × both themes from a single declaration: [ScreenThemePreviews] supplies the
 * light/dark pair on a phone canvas, [HomePreviewCases] supplies the states.
 */
@ScreenThemePreviews
@Composable
private fun HomeScreenContentPreview(
    @PreviewParameter(HomePreviewCases::class) case: HomePreviewCase,
) {
    NativeMindsTheme {
        val lessons = flowOf(PagingData.from(case.lessons)).collectAsLazyPagingItems()
        HomeScreenContent(
            state = case.state,
            lessons = lessons,
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onLessonClick = { _, _, _ -> },
            onProfileClick = {},
        )
    }
}

/**
 * All three time-of-day variants at once — the greeting label is the only thing that changes, and
 * seeing them stacked catches a label that no longer fits next to the avatar.
 */
@ThemePreviews
@Composable
private fun GreetingHeaderPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            GreetingPeriod.entries.forEach { period ->
                GreetingHeader(greeting = period, userName = "Ozan", onProfileClick = {})
            }
        }
    }
}
