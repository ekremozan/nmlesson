package com.example.nativeminds.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.home.R

/**
 * The pill search field on Home — border tints to `primary` while a query is active, matching the
 * design's "actively filtering" state (see screen `1c`).
 */
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasQuery = query.isNotEmpty()
    val borderColor = if (hasQuery) MaterialTheme.colorScheme.primary else NativeMindsTheme.colors.cardBorder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(Pill)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, borderColor, Pill)
            .padding(horizontal = NativeMindsTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
    ) {
        NativeMindsIcons.Search(tint = MaterialTheme.colorScheme.primary, size = 19.dp)

        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_search_placeholder),
                    style = NativeMindsTheme.typography.searchInput,
                    color = NativeMindsTheme.colors.textMuted,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = NativeMindsTheme.typography.searchInput.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (hasQuery) {
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(NativeMindsTheme.colors.cardBorder),
            ) {
                NativeMindsIcons.Close(tint = MaterialTheme.colorScheme.onSurface, size = 13.dp)
            }
        }
    }
}
