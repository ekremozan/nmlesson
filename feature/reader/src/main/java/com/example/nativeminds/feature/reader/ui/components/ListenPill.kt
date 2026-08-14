package com.example.nativeminds.feature.reader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.reader.R

private const val PERCENT_COMPLETE = 100

/**
 * The floating control at the foot of the reader: a listen action and the reading progress.
 *
 * The progress bar tracks the reader's position **in the text**, not in an audio track — playback
 * belongs to a later feature. The listen action is drawn as designed and answers a tap by saying
 * so, which is the honest version of a control that cannot work yet.
 */
@Composable
fun ListenPill(
    progressPercent: Int,
    remainingMinutes: Int,
    onListenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(NativeMindsTheme.sizes.listenPill)
            .clip(Pill)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, NativeMindsTheme.colors.cardBorder, Pill)
            .padding(horizontal = NativeMindsTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier
                .height(NativeMindsTheme.sizes.listenPillAction)
                .clip(Pill)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onListenClick)
                .padding(horizontal = NativeMindsTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
        ) {
            NativeMindsIcons.Play(tint = MaterialTheme.colorScheme.onPrimary)
            Text(
                text = stringResource(R.string.reader_listen),
                style = NativeMindsTheme.typography.actionLabel,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = NativeMindsTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.reader_progress, progressPercent),
                    style = NativeMindsTheme.typography.progressLabel,
                    color = NativeMindsTheme.colors.textMuted,
                )
                Text(
                    text = stringResource(R.string.reader_remaining, remainingMinutes),
                    style = NativeMindsTheme.typography.progressLabel,
                    color = NativeMindsTheme.colors.textMuted,
                )
            }

            ProgressTrack(progressPercent = progressPercent)
        }
    }
}

@Composable
private fun ProgressTrack(progressPercent: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(NativeMindsTheme.sizes.progressTrack)
            .clip(Pill)
            .background(MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progressPercent.toFloat() / PERCENT_COMPLETE)
                .height(NativeMindsTheme.sizes.progressTrack)
                .clip(Pill)
                .background(NativeMindsTheme.colors.readingProgress),
        )
    }
}

@ThemePreviews
@Composable
private fun ListenPillPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ListenPill(progressPercent = 0, remainingMinutes = 6, onListenClick = {})
            ListenPill(progressPercent = 34, remainingMinutes = 4, onListenClick = {})
            ListenPill(progressPercent = 100, remainingMinutes = 0, onListenClick = {})
        }
    }
}
