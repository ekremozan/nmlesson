package com.example.nativeminds.feature.reader.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
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
import com.example.nativeminds.feature.reader.ui.model.ListenPillStatus
import kotlin.math.roundToInt

private const val PERCENT_COMPLETE = 100

/** Roughly how long one word takes to speak, so the fill arrives as the next word begins. */
private const val FILL_MILLIS = 300

/**
 * The floating control at the foot of the reader: a listen action and how far narration has got.
 *
 * The bar is a measure of **listening**, taken from the words spoken rather than from the scroll
 * position: the two are independent ways through a story, and a bar that switched between them
 * would jump every time the reader scrolled ahead of the voice. It therefore sits at zero until
 * narration starts.
 *
 * [progress] arrives as a fraction rather than whole percent so the bar keeps moving on stories
 * long enough that a single word is worth less than one percent of them.
 */
@Composable
fun ListenPill(
    progress: Float,
    remainingMinutes: Int,
    status: ListenPillStatus,
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
            if (status == ListenPillStatus.PLAYING) {
                NativeMindsIcons.Pause(tint = MaterialTheme.colorScheme.onPrimary)
            } else {
                NativeMindsIcons.Play(tint = MaterialTheme.colorScheme.onPrimary)
            }
            Text(
                text = stringResource(status.labelRes()),
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
                    text = stringResource(
                        R.string.reader_progress,
                        (progress * PERCENT_COMPLETE).roundToInt(),
                    ),
                    style = NativeMindsTheme.typography.progressLabel,
                    color = NativeMindsTheme.colors.textMuted,
                )
                Text(
                    text = stringResource(R.string.reader_remaining, remainingMinutes),
                    style = NativeMindsTheme.typography.progressLabel,
                    color = NativeMindsTheme.colors.textMuted,
                )
            }

            ProgressTrack(progress = progress)
        }
    }
}

private fun ListenPillStatus.labelRes(): Int = when (this) {
    ListenPillStatus.IDLE -> R.string.reader_listen
    ListenPillStatus.PLAYING -> R.string.reader_pause
    ListenPillStatus.PAUSED -> R.string.reader_resume
}

/**
 * Each word nudges [progress] by a fraction of a percent; animating between those steps is what
 * makes the bar read as travelling with the voice instead of ticking.
 */
@Composable
private fun ProgressTrack(progress: Float) {
    val filled by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(FILL_MILLIS, easing = LinearEasing),
        label = "narrationProgress",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(NativeMindsTheme.sizes.progressTrack)
            .clip(Pill)
            .background(MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(filled)
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
            ListenPill(
                progress = 0f,
                remainingMinutes = 6,
                status = ListenPillStatus.IDLE,
                onListenClick = {},
            )
            ListenPill(
                progress = 0.34f,
                remainingMinutes = 4,
                status = ListenPillStatus.PLAYING,
                onListenClick = {},
            )
            ListenPill(
                progress = 0.34f,
                remainingMinutes = 4,
                status = ListenPillStatus.PAUSED,
                onListenClick = {},
            )
            ListenPill(
                progress = 1f,
                remainingMinutes = 0,
                status = ListenPillStatus.IDLE,
                onListenClick = {},
            )
        }
    }
}
