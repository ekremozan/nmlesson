package com.example.nativeminds.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.home.R
import com.example.nativeminds.feature.home.ui.model.ChipUiModel
import com.example.nativeminds.feature.home.ui.preview.PreviewChips

/** Horizontally scrolling row of selectable category chips — used for the filter row on Home. */
@Composable
fun CategoryChipRow(
    chips: List<ChipUiModel>,
    onChipSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
    ) {
        chips.forEach { chip ->
            CategoryChip(chip = chip, onClick = { onChipSelected(chip.category) })
        }
    }
}

/** A single chip — also reused for the empty-state search suggestions (unselected, outlined). */
@Composable
fun CategoryChip(
    chip: ChipUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (chip.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val border = if (chip.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val content = if (chip.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Text(
        // A category is content and renders as-is; the null "All" chip is UI text and is localized.
        text = chip.category ?: stringResource(R.string.category_all),
        style = NativeMindsTheme.typography.chip,
        color = content,
        modifier = modifier
            .clip(Pill)
            .background(background)
            .border(1.dp, border, Pill)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}

@ThemePreviews
@Composable
private fun CategoryChipRowPreview() {
    PreviewSurface {
        CategoryChipRow(chips = PreviewChips, onChipSelected = {})
    }
}

/** Both chip states side by side — selected (filled) and unselected (outlined). */
@ThemePreviews
@Composable
private fun CategoryChipPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryChip(chip = ChipUiModel(category = "Fiction", isSelected = true), onClick = {})
            CategoryChip(chip = ChipUiModel(category = "History"), onClick = {})
            CategoryChip(chip = ChipUiModel(category = null), onClick = {})
        }
    }
}
