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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.reader.R

private const val BADGE_SIZE_DP = 48
private const val BADGE_ICON_SIZE_DP = 21
private const val CARD_ELEVATION_DP = 8
private const val CHECK_CIRCLE_SIZE_DP = 22
private const val CHECK_ICON_SIZE_DP = 12

/**
 * The card that replaces the withheld part of a restricted lesson: what the reader has read so
 * far, what premium adds, and the way into the paywall. [onUnlockRequested] is the same callback
 * that used to sit behind a plain pill, so tapping it still means "take me to the paywall" —
 * only what earns the tap changed.
 */
@Composable
fun UnlockCard(freeSharePercent: Int, onUnlockRequested: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.lg),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = CARD_ELEVATION_DP.dp, shape = MaterialTheme.shapes.large, clip = false)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(1.dp, NativeMindsTheme.colors.cardBorder, MaterialTheme.shapes.large)
                .padding(
                    top = NativeMindsTheme.spacing.screen,
                    start = NativeMindsTheme.spacing.lg,
                    end = NativeMindsTheme.spacing.lg,
                    bottom = NativeMindsTheme.spacing.lg,
                ),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm)) {
                Text(
                    text = stringResource(R.string.reader_unlock_card_title),
                    style = NativeMindsTheme.typography.unlockTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.reader_unlock_card_body, freeSharePercent),
                    style = NativeMindsTheme.typography.unlockBody,
                    color = NativeMindsTheme.colors.textMuted,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm)) {
                UnlockBenefitRow(text = stringResource(R.string.reader_unlock_card_benefit_unlimited))
                UnlockBenefitRow(text = stringResource(R.string.reader_unlock_card_benefit_audio))
                UnlockBenefitRow(text = stringResource(R.string.reader_unlock_card_benefit_ai))
            }

            Column {
                Text(
                    text = stringResource(R.string.reader_unlock_card_cta),
                    style = NativeMindsTheme.typography.actionLabel,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(NativeMindsTheme.sizes.actionButton)
                        .clip(Pill)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onUnlockRequested)
                        .padding(vertical = NativeMindsTheme.spacing.md),
                )
                Text(
                    text = stringResource(R.string.reader_unlock_card_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = NativeMindsTheme.colors.textSubtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = NativeMindsTheme.spacing.sm),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = NativeMindsTheme.spacing.screen, y = -(BADGE_SIZE_DP / 2).dp)
                .size(BADGE_SIZE_DP.dp)
                .shadow(elevation = CARD_ELEVATION_DP.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Lock(
                tint = MaterialTheme.colorScheme.onPrimary,
                size = BADGE_ICON_SIZE_DP.dp,
            )
        }
    }
}

@Composable
private fun UnlockBenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(CHECK_CIRCLE_SIZE_DP.dp)
                .clip(CircleShape)
                .background(NativeMindsTheme.colors.benefitCheckBackground),
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Check(
                tint = NativeMindsTheme.colors.benefitCheckContent,
                size = CHECK_ICON_SIZE_DP.dp,
            )
        }
        Text(
            text = text,
            style = NativeMindsTheme.typography.unlockBenefit,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
