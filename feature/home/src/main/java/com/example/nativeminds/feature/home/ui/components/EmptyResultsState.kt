package com.example.nativeminds.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.feature.home.R
import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.preview.PreviewSuggestions

/**
 * The empty branch of Home's lesson list — search field stays live above this (see [HomeScreen]).
 * Covers two distinct cases behind one layout: [isFiltering] `true` is "no results for this
 * search/subject" (a tapped suggestion re-filters rather than navigating anywhere, design `1a`);
 * `false` is "this device has never synced any content", where a query/suggestions/clear-search
 * would all be meaningless, so that row and link are omitted instead of shown empty.
 *
 * [onSuggestionSelected] is deliberately not the filter row's handler: a suggestion means "show me
 * this subject instead", so it also clears the query that found nothing.
 */
@Composable
fun EmptyResultsState(
    isFiltering: Boolean,
    query: String,
    suggestions: List<ChipUiModel>,
    onSuggestionSelected: (String?) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 34.dp, horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Search(tint = MaterialTheme.colorScheme.primary, size = 40.dp)
        }

        Text(
            text = stringResource(
                if (isFiltering) R.string.home_empty_title else R.string.home_never_synced_title,
            ),
            style = NativeMindsTheme.typography.emptyTitle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = if (isFiltering) {
                stringResource(R.string.home_empty_body, query)
            } else {
                stringResource(R.string.home_never_synced_body)
            },
            style = NativeMindsTheme.typography.emptyBody,
            color = NativeMindsTheme.colors.textMuted,
            textAlign = TextAlign.Center,
        )

        if (isFiltering) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                suggestions.forEach { suggestion ->
                    SubjectChip(chip = suggestion, onClick = { onSuggestionSelected(suggestion.subject) })
                }
            }

            Text(
                text = stringResource(R.string.home_clear_search),
                style = NativeMindsTheme.typography.chip,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClearSearch,
                    ),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun EmptyResultsStatePreview() {
    PreviewSurface {
        EmptyResultsState(
            isFiltering = true,
            query = "quantum lullabies",
            suggestions = PreviewSuggestions,
            onSuggestionSelected = {},
            onClearSearch = {},
        )
    }
}

@ThemePreviews
@Composable
private fun NeverSyncedEmptyStatePreview() {
    PreviewSurface {
        EmptyResultsState(
            isFiltering = false,
            query = "",
            suggestions = emptyList(),
            onSuggestionSelected = {},
            onClearSearch = {},
        )
    }
}
