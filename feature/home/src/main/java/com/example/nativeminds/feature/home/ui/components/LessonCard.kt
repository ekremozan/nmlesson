package com.example.nativeminds.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.icons.SubjectImageAssets
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.feature.home.R
import com.example.nativeminds.feature.home.ui.model.LessonUiModel
import com.example.nativeminds.feature.home.ui.preview.PreviewLockedLesson
import com.example.nativeminds.feature.home.ui.preview.PreviewLesson

/**
 * The lesson card in the Home list — cover, optional "Premium" overlay when locked, subject kicker,
 * title, teaser, and the minutes/audio meta row.
 */
@Composable
fun LessonCard(
    lesson: LessonUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardBackground = if (NativeMindsTheme.colors.isDark) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(cardBackground)
            .border(1.dp, NativeMindsTheme.colors.cardBorder, MaterialTheme.shapes.medium)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(NativeMindsTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.lg),
    ) {
        LessonCover(lesson = lesson)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = lesson.subject.uppercase(),
                style = NativeMindsTheme.typography.lessonSubject,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = lesson.title,
                style = NativeMindsTheme.typography.lessonTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = lesson.teaser,
                style = NativeMindsTheme.typography.lessonTeaser,
                color = NativeMindsTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pluralStringResource(R.plurals.home_lesson_minutes, lesson.minutes, lesson.minutes),
                    style = NativeMindsTheme.typography.lessonMeta,
                    color = NativeMindsTheme.colors.textMuted,
                    fontWeight = FontWeight.SemiBold,
                )
                if (lesson.hasAudio) {
                    NativeMindsIcons.Headphones(tint = MaterialTheme.colorScheme.secondary, size = 14.dp)
                }
            }
        }
    }
}

@Composable
private fun LessonCover(lesson: LessonUiModel) {
    Box(modifier = Modifier.size(88.dp)) {
        Image(
            painter = painterResource(SubjectImageAssets.resolve(lesson.image)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(88.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(NativeMindsTheme.colors.cover)
                .alpha(if (lesson.isLocked) NativeMindsTheme.colors.lockedCoverAlpha else 1f),
        )

        if (lesson.isLocked) {
            Row(
                modifier = Modifier
                    .padding(6.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(NativeMindsTheme.colors.premiumBadgeBackground)
                    .padding(start = 6.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                NativeMindsIcons.Lock(tint = NativeMindsTheme.colors.premiumBadgeContent, size = 11.dp)
                Text(
                    text = stringResource(R.string.home_premium_badge).uppercase(),
                    style = NativeMindsTheme.typography.premiumBadge,
                    color = NativeMindsTheme.colors.premiumBadgeContent,
                )
            }
        }
    }
}

/** Free lesson (audio meta) above a locked one (dimmed cover + Premium badge, no audio). */
@ThemePreviews
@Composable
private fun LessonCardPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LessonCard(lesson = PreviewLesson, onClick = {})
            LessonCard(lesson = PreviewLockedLesson, onClick = {})
        }
    }
}

@ThemePreviews
@Composable
private fun LessonCoverPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LessonCover(lesson = PreviewLesson)
            LessonCover(lesson = PreviewLockedLesson)
        }
    }
}
