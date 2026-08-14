package com.example.nativeminds.feature.reader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.reader.R
import com.example.nativeminds.feature.reader.ui.model.ReaderBodyUiModel
import com.example.nativeminds.feature.reader.ui.model.ReaderStoryUiModel
import com.example.nativeminds.feature.reader.ui.preview.PreviewFullBody
import com.example.nativeminds.feature.reader.ui.preview.PreviewFreeStory
import com.example.nativeminds.feature.reader.ui.preview.PreviewPremiumStory
import com.example.nativeminds.feature.reader.ui.preview.PreviewTruncatedBody

private const val COVER_GLYPH_SIZE_DP = 30

/**
 * The story itself.
 *
 * A `LazyColumn` of paragraphs rather than one long `Text`: a full-length story stays smooth to
 * scroll, and the list restores its position across a configuration change with nothing to save by
 * hand. The header and the closing note are items in the same list so they scroll with the text
 * instead of pinning to the top.
 *
 * When the body is truncated the composable only ever receives the paragraphs the reader is
 * allowed to see — the withheld ones never reach it, so there is nothing here to accidentally
 * draw, select, or read aloud.
 */
@Composable
fun ReaderBody(
    story: ReaderStoryUiModel,
    body: ReaderBodyUiModel,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.lg),
    ) {
        item(key = "header") {
            StoryHeader(story = story, showCover = !body.isTruncated)
        }

        itemsIndexed(
            items = body.paragraphs,
            key = { index, _ -> "paragraph-$index" },
        ) { index, paragraph ->
            Paragraph(text = paragraph, isFirst = index == 0)
        }

        if (!body.isTruncated) {
            item(key = "closing") {
                ClosingNote(category = story.category)
            }
        }
    }
}

@Composable
private fun StoryHeader(story: ReaderStoryUiModel, showCover: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (story.isPremium) {
                PremiumChip()
            }
            Text(
                text = stringResource(R.string.reader_kicker, story.category, story.minutes)
                    .uppercase(),
                style = NativeMindsTheme.typography.storyCategory,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.md))

        Text(
            text = story.title,
            style = NativeMindsTheme.typography.readerTitle,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.sm))

        Text(
            text = stringResource(R.string.reader_by_author, story.author),
            style = NativeMindsTheme.typography.readerAuthor,
            color = NativeMindsTheme.colors.textMuted,
        )

        if (showCover) {
            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NativeMindsTheme.sizes.readerCover)
                    .clip(MaterialTheme.shapes.medium)
                    .background(NativeMindsTheme.colors.cover),
                contentAlignment = Alignment.Center,
            ) {
                NativeMindsIcons.ImagePlaceholder(
                    tint = NativeMindsTheme.colors.textSubtle,
                    size = COVER_GLYPH_SIZE_DP.dp,
                )
            }
        }
    }
}

@Composable
private fun PremiumChip() {
    Row(
        modifier = Modifier
            .clip(Pill)
            .background(NativeMindsTheme.colors.premiumChipBackground)
            .padding(
                horizontal = NativeMindsTheme.spacing.sm,
                vertical = NativeMindsTheme.spacing.xs,
            ),
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NativeMindsIcons.Lock(tint = NativeMindsTheme.colors.premiumChipContent)
        Text(
            text = stringResource(R.string.reader_premium_badge).uppercase(),
            style = NativeMindsTheme.typography.premiumBadge,
            color = NativeMindsTheme.colors.premiumChipContent,
        )
    }
}

/**
 * The first paragraph carries a drop cap, drawn as a styled span rather than as a separate
 * composable so the rest of the sentence still flows as one block of text.
 */
@Composable
private fun Paragraph(text: String, isFirst: Boolean) {
    val dropCap = NativeMindsTheme.typography.readerDropCap
    val accent = NativeMindsTheme.colors.accentText

    val rendered = if (isFirst && text.isNotEmpty()) {
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontFamily = dropCap.fontFamily,
                    fontSize = dropCap.fontSize,
                    color = accent,
                ),
            ) {
                append(text.first())
            }
            append(text.drop(1))
        }
    } else {
        buildAnnotatedString { append(text) }
    }

    Text(
        text = rendered,
        style = NativeMindsTheme.typography.readingBody,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun ClosingNote(category: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NativeMindsTheme.sizes.readerClosingRuleInset)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

        Text(
            text = stringResource(R.string.reader_closing_note, category),
            style = NativeMindsTheme.typography.readerClosingNote,
            color = NativeMindsTheme.colors.textMuted,
        )
    }
}

@ThemePreviews
@Composable
private fun ReaderBodyFullPreview() {
    PreviewSurface {
        ReaderBody(
            story = PreviewFreeStory,
            body = PreviewFullBody,
            listState = rememberLazyListState(),
            contentPadding = PaddingValues(0.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun ReaderBodyTruncatedPreview() {
    PreviewSurface {
        ReaderBody(
            story = PreviewPremiumStory,
            body = PreviewTruncatedBody,
            listState = rememberLazyListState(),
            contentPadding = PaddingValues(0.dp),
        )
    }
}
