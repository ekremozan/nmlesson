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
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.feature.home.R
import com.example.nativeminds.feature.home.ui.model.ChipUiModel

/**
 * The "no results" branch of Home — search field stays live above this (see [HomeScreen]), and a
 * tapped suggestion re-filters by that category rather than navigating anywhere (design `1a`).
 */
@Composable
fun EmptyResultsState(
    query: String,
    suggestions: List<ChipUiModel>,
    onSuggestionSelected: (String) -> Unit,
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
            text = stringResource(R.string.home_empty_title),
            style = NativeMindsTheme.typography.emptyTitle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(R.string.home_empty_body, query),
            style = NativeMindsTheme.typography.emptyBody,
            color = NativeMindsTheme.colors.textMuted,
            textAlign = TextAlign.Center,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.padding(top = 6.dp),
        ) {
            suggestions.forEach { suggestion ->
                CategoryChip(chip = suggestion, onClick = { onSuggestionSelected(suggestion.label) })
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
