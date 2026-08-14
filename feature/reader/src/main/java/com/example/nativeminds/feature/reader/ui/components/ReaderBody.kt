package com.example.nativeminds.feature.reader.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.icons.StoryCoverAssets
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.reader.R
import com.example.nativeminds.feature.reader.ui.NarrationHighlight
import com.example.nativeminds.feature.reader.ui.model.ReaderBodyUiModel
import com.example.nativeminds.feature.reader.ui.model.ReaderStoryUiModel
import com.example.nativeminds.feature.reader.ui.preview.PreviewFullBody
import com.example.nativeminds.feature.reader.ui.preview.PreviewFreeStory
import com.example.nativeminds.feature.reader.ui.preview.PreviewPremiumStory
import com.example.nativeminds.feature.reader.ui.preview.PreviewTruncatedBody

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
    highlight: NarrationHighlight? = null,
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
            Paragraph(
                text = paragraph,
                isFirst = index == 0,
                highlight = highlight?.takeIf { it.paragraphIndex == index },
            )
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
            Image(
                painter = painterResource(StoryCoverAssets.resolve(story.image)),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NativeMindsTheme.sizes.readerCover)
                    .clip(MaterialTheme.shapes.medium)
                    .background(NativeMindsTheme.colors.cover),
            )
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
 *
 * The narration highlight is painted from the measured layout instead of as a
 * `SpanStyle(background)`. The drop cap makes its line far taller than the reading line height,
 * and the platform's span backgrounds are placed against that inflated line rather than against
 * the text — in the opening paragraph they landed a third of a line low and drew over the very
 * words they were meant to mark. Anchoring each run to its own baseline is exact on every line of
 * every paragraph, and `drawBehind` puts it behind the glyphs by construction.
 */
@Composable
private fun Paragraph(text: String, isFirst: Boolean, highlight: NarrationHighlight?) {
    val dropCap = NativeMindsTheme.typography.readerDropCap
    val accent = NativeMindsTheme.colors.accentText
    val body = NativeMindsTheme.typography.readingBody
    val highlightColor = NativeMindsTheme.colors.narrationHighlight
    val highlightPadding = NativeMindsTheme.sizes.narrationHighlightPadding
    val cornerRadius = NativeMindsTheme.sizes.narrationHighlightRadius

    val rendered = remember(text, isFirst, dropCap, accent) {
        buildAnnotatedString {
            append(text)
            if (isFirst && text.isNotEmpty()) {
                addStyle(
                    SpanStyle(
                        fontFamily = dropCap.fontFamily,
                        fontSize = dropCap.fontSize,
                        color = accent,
                    ),
                    start = 0,
                    end = 1,
                )
            }
        }
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current

    val marks = remember(layout, highlight, density, body, highlightPadding) {
        val measured = layout
        val range = highlight
        if (measured == null || range == null) {
            emptyList()
        } else {
            with(density) {
                measured.narrationMarks(
                    start = range.start.coerceIn(0, text.length),
                    end = range.end.coerceIn(0, text.length),
                    fontSize = body.fontSize.toPx(),
                    padding = highlightPadding.toPx(),
                )
            }
        }
    }

    val glide = rememberGlidingMark(marks)

    Text(
        text = rendered,
        style = body,
        color = MaterialTheme.colorScheme.onBackground,
        onTextLayout = { layout = it },
        modifier = Modifier.drawBehind {
            val radius = CornerRadius(cornerRadius.toPx())
            val drawn = glide?.let(::listOf) ?: marks
            drawn.forEach { mark ->
                drawRoundRect(
                    color = highlightColor,
                    topLeft = mark.topLeft,
                    size = mark.size,
                    cornerRadius = radius,
                )
            }
        },
    )
}

/**
 * The word-to-word glide the design asks for, applied only when the highlight stays on one line.
 *
 * Wrapping to the next line is a jump, not a movement: animating it would send the mark sweeping
 * backwards across the whole column while the voice is already saying the next word. The
 * multi-line marks of a whole-sentence highlight are drawn as they are for the same reason.
 */
@Composable
private fun rememberGlidingMark(marks: List<Rect>): Rect? {
    val single = marks.singleOrNull()
    val mark = remember { Animatable(Rect.Zero, Rect.VectorConverter) }

    LaunchedEffect(single) {
        if (single == null) return@LaunchedEffect
        if (mark.value.height == 0f || mark.value.top != single.top) {
            mark.snapTo(single)
        } else {
            mark.animateTo(single, tween(GLIDE_MILLIS, easing = GlideEasing))
        }
    }

    return single?.let { if (mark.value.height == 0f) it else mark.value }
}

private const val GLIDE_MILLIS = 180
private val GlideEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

/**
 * How far the mark reaches above and below the baseline before padding, as a share of the font
 * size. Taken from the text's own baseline rather than the line box, which the drop cap inflates.
 */
private const val MARK_RISE = 0.92f
private const val MARK_DROP = 0.28f

private fun TextLayoutResult.narrationMarks(
    start: Int,
    end: Int,
    fontSize: Float,
    padding: Float,
): List<Rect> {
    if (start >= end) return emptyList()

    return (getLineForOffset(start)..getLineForOffset(end - 1)).mapNotNull { line ->
        val from = maxOf(start, getLineStart(line))
        val to = minOf(end, getLineEnd(line, visibleEnd = true))
        if (from >= to) return@mapNotNull null

        val left = getHorizontalPosition(from, usePrimaryDirection = true)
        val right = getHorizontalPosition(to, usePrimaryDirection = true)
        val baseline = getLineBaseline(line)

        Rect(
            left = minOf(left, right) - padding,
            top = baseline - fontSize * MARK_RISE - padding,
            right = maxOf(left, right) + padding,
            bottom = baseline + fontSize * MARK_DROP + padding,
        )
    }
}

/**
 * Where a paragraph sits in the list, for a caller that wants to scroll to it — the header is an
 * item of this list too, so paragraph and item index are not the same number.
 */
internal fun paragraphItemIndex(paragraphIndex: Int): Int = paragraphIndex + 1

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
private fun ReaderBodyNarratingPreview() {
    val word = "clockwork"
    val paragraphIndex = 1
    val wordStart = PreviewFullBody.paragraphs[paragraphIndex].indexOf(word)

    PreviewSurface {
        ReaderBody(
            story = PreviewFreeStory,
            body = PreviewFullBody,
            listState = rememberLazyListState(),
            contentPadding = PaddingValues(0.dp),
            highlight = NarrationHighlight(paragraphIndex, wordStart, wordStart + word.length),
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
