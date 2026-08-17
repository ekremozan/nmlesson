package com.example.nativeminds.feature.reader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.reader.R

private val TestActionHeight = 34.dp
private val TestActionIconSize = 15.dp
private const val TestActionBackgroundAlpha = 0.14f

/**
 * The reader's chrome: back, the lesson's title repeated small and muted, and — for a lesson the
 * reader has full access to — the "Test" action that opens an AI-generated quiz question.
 *
 * The design also puts an overflow control on the right. It is left out rather than drawn inert —
 * everything behind it (font size, theme) belongs to a later feature, and a button that answers a
 * tap with nothing is the most misleading thing a screen can contain. A spacer of the same width
 * keeps the title optically centred when neither trailing control is shown.
 *
 * The title truncates rather than wrapping — the bar is a fixed height and a two-line title here
 * would push the lesson itself down the page.
 */
@Composable
fun ReaderTopBar(
    title: String,
    onBack: () -> Unit,
    showTest: Boolean,
    onTestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(NativeMindsTheme.sizes.readerTopBar)
            .padding(horizontal = NativeMindsTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconAction(
            contentDescription = stringResource(R.string.reader_back),
            onClick = onBack,
        ) {
            NativeMindsIcons.ChevronLeft(tint = MaterialTheme.colorScheme.onBackground)
        }

        Text(
            text = title,
            style = NativeMindsTheme.typography.readerTopBarTitle,
            color = NativeMindsTheme.colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = NativeMindsTheme.spacing.sm),
        )

        if (showTest) {
            TestAction(onClick = onTestClick)
        } else {
            Spacer(modifier = Modifier.size(NativeMindsTheme.sizes.iconButton))
        }
    }
}

/** "Test" pill — opens the AI-generated quiz for the lesson currently on screen. */
@Composable
private fun TestAction(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.xs),
        modifier = Modifier
            .height(TestActionHeight)
            .clip(Pill)
            .background(NativeMindsTheme.colors.accentText.copy(alpha = TestActionBackgroundAlpha))
            .clickable(onClick = onClick)
            .padding(horizontal = NativeMindsTheme.spacing.md, vertical = NativeMindsTheme.spacing.xs),
    ) {
        NativeMindsIcons.Sparkle(tint = NativeMindsTheme.colors.accentText, size = TestActionIconSize)
        Text(
            text = stringResource(R.string.reader_test),
            style = NativeMindsTheme.typography.chip,
            color = NativeMindsTheme.colors.accentText,
        )
    }
}

@Composable
private fun IconAction(
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(NativeMindsTheme.sizes.iconButton)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@ThemePreviews
@Composable
private fun ReaderTopBarPreview() {
    PreviewSurface(padding = 0.dp) {
        Column {
            ReaderTopBar(
                title = "The Lighthouse Keeper's Last Letter",
                onBack = {},
                showTest = true,
                onTestClick = {},
            )
            ReaderTopBar(
                title = "The Cartographer of Missing Islands",
                onBack = {},
                showTest = false,
                onTestClick = {},
            )
        }
    }
}
