package com.example.nativeminds.feature.reader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.domain.model.UnavailableReason
import com.example.nativeminds.feature.reader.R
import com.example.nativeminds.feature.reader.ui.preview.UnavailableReasons

/**
 * Why the lesson is not on screen, and what to do about it.
 *
 * One composable for all three reasons rather than three near-identical screens: they differ only
 * in their words and in whether retrying can help, and keeping them together is what stops one of
 * them from quietly drifting into a worse version of the others.
 *
 * A missing lesson offers no retry — nothing about trying again would bring it back.
 */
@Composable
fun ReaderUnavailableState(
    reason: UnavailableReason,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (reason) {
        UnavailableReason.OFFLINE -> R.string.reader_offline_title
        UnavailableReason.ERROR -> R.string.reader_error_title
        UnavailableReason.LESSON_MISSING -> R.string.reader_missing_title
    }
    val body = when (reason) {
        UnavailableReason.OFFLINE -> R.string.reader_offline_body
        UnavailableReason.ERROR -> R.string.reader_error_body
        UnavailableReason.LESSON_MISSING -> R.string.reader_missing_body
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(NativeMindsTheme.spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
    ) {
        Text(
            text = stringResource(title),
            style = NativeMindsTheme.typography.emptyTitle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(body),
            style = NativeMindsTheme.typography.emptyBody,
            color = NativeMindsTheme.colors.textMuted,
            textAlign = TextAlign.Center,
        )

        if (reason != UnavailableReason.LESSON_MISSING) {
            Text(
                text = stringResource(R.string.reader_retry),
                style = NativeMindsTheme.typography.actionLabel,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = NativeMindsTheme.spacing.sm)
                    .clip(Pill)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onRetry)
                    .padding(
                        horizontal = NativeMindsTheme.spacing.xl,
                        vertical = NativeMindsTheme.spacing.md,
                    ),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ReaderUnavailableStatePreview(
    @PreviewParameter(UnavailableReasons::class) reason: UnavailableReason,
) {
    PreviewSurface {
        ReaderUnavailableState(reason = reason, onRetry = {})
    }
}
