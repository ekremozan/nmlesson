package com.example.nativeminds.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.nativeminds.feature.home.ui.components.CategoryChipRow
import com.example.nativeminds.feature.home.ui.components.EmptyResultsState
import com.example.nativeminds.feature.home.ui.components.HomeBottomNavBar
import com.example.nativeminds.feature.home.ui.components.SearchField
import com.example.nativeminds.feature.home.ui.components.StoryCard
import com.example.nativeminds.feature.home.ui.model.GreetingPeriod
import com.example.nativeminds.feature.home.ui.model.StoryUiModel
import com.example.nativeminds.feature.home.ui.preview.HomePreviewCase
import com.example.nativeminds.feature.home.ui.preview.HomePreviewCases
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stories = viewModel.pagedStories.collectAsLazyPagingItems()
    HomeScreenContent(
        state = state,
        stories = stories,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::onClearQuery,
        onCategorySelected = viewModel::onCategorySelected,
        onSuggestionSelected = viewModel::onSuggestionSelected,
        modifier = modifier,
    )
}

/** Stateless so it's directly usable from `@Preview`s and future tests. */
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    stories: LazyPagingItems<StoryUiModel>,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onSuggestionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEmpty = stories.itemCount == 0 && stories.loadState.refresh is LoadState.NotLoading

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(horizontal = NativeMindsTheme.spacing.screen),
                verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.lg),
            ) {
                GreetingHeader(greeting = state.greeting, userName = state.userName)
                SearchField(query = state.query, onQueryChange = onQueryChange, onClear = onClearQuery)
            }

            CategoryChipRow(
                chips = state.chips,
                onChipSelected = onCategorySelected,
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
                    text = pluralStringResource(R.plurals.home_story_count, stories.itemCount, stories.itemCount),
                    style = NativeMindsTheme.typography.sectionCount,
                    color = NativeMindsTheme.colors.textSubtle,
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 108.dp),
            ) {
                if (isEmpty) {
                    item {
                        EmptyResultsState(
                            query = state.query,
                            suggestions = state.suggestions,
                            onSuggestionSelected = onSuggestionSelected,
                            onClearSearch = onClearQuery,
                        )
                    }
                } else {
                    items(count = stories.itemCount, key = stories.itemKey { it.id }) { index ->
                        val story = stories[index] ?: return@items
                        StoryCard(
                            story = story,
                            onClick = {},
                            modifier = Modifier.padding(
                                horizontal = NativeMindsTheme.spacing.screen,
                                vertical = 7.dp,
                            ),
                        )
                    }
                }
            }
        }

        HomeBottomNavBar(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun GreetingHeader(greeting: GreetingPeriod, userName: String) {
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
                .border(1.dp, NativeMindsTheme.colors.cardBorder, CircleShape),
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
        val stories = flowOf(PagingData.from(case.stories)).collectAsLazyPagingItems()
        HomeScreenContent(case.state, stories, {}, {}, {}, {})
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
                GreetingHeader(greeting = period, userName = "Ozan")
            }
        }
    }
}
