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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.feature.home.R
import com.example.nativeminds.feature.home.ui.model.StoryUiModel

/**
 * The story card in the Home list — cover, optional "Premium" overlay when locked, category kicker,
 * title, teaser, and the minutes/audio meta row.
 */
@Composable
fun StoryCard(
    story: StoryUiModel,
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
        StoryCover(story = story)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = story.category.uppercase(),
                style = NativeMindsTheme.typography.storyCategory,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = story.title,
                style = NativeMindsTheme.typography.storyTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = story.teaser,
                style = NativeMindsTheme.typography.storyTeaser,
                color = NativeMindsTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = story.minutesLabel,
                    style = NativeMindsTheme.typography.storyMeta,
                    color = NativeMindsTheme.colors.textMuted,
                    fontWeight = FontWeight.SemiBold,
                )
                if (story.hasAudio) {
                    NativeMindsIcons.Headphones(tint = MaterialTheme.colorScheme.secondary, size = 14.dp)
                }
            }
        }
    }
}

@Composable
private fun StoryCover(story: StoryUiModel) {
    Box(modifier = Modifier.size(88.dp)) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(NativeMindsTheme.colors.cover)
                .alpha(if (story.isLocked) NativeMindsTheme.colors.lockedCoverAlpha else 1f),
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.ImagePlaceholder(tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 26.dp)
        }

        if (story.isLocked) {
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
