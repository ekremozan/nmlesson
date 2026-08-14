package com.example.nativeminds.feature.reader.ui.components

import androidx.annotation.StringRes
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.PreviewSurface
import com.example.nativeminds.designsystem.preview.ThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.reader.R

private const val MEDALLION_GLYPH_SIZE_DP = 21

/**
 * The premium wall, as a bottom sheet rather than the anchored card the design draws.
 *
 * A sheet is what the interaction actually is: it arrives over the story, it can be pushed away,
 * and it comes back. Drawn as a card it would either sit there permanently — covering text the
 * reader is allowed to see — or need its own dismissal affordance invented from nothing.
 *
 * The wrapper itself has no preview: `ModalBottomSheet` renders nothing in the preview tool. Its
 * contents are previewed instead, which is where all of the design lives.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumUnlockSheet(
    freeSharePercent: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier,
    ) {
        PremiumUnlockContent(
            freeSharePercent = freeSharePercent,
            onSubscribe = onSubscribe,
        )
    }
}

/**
 * The sheet's contents, separated from the sheet itself so a preview can render them — a
 * `ModalBottomSheet` shows nothing in the preview tool.
 */
@Composable
private fun PremiumUnlockContent(
    freeSharePercent: Int,
    onSubscribe: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = NativeMindsTheme.spacing.screen,
                end = NativeMindsTheme.spacing.screen,
                bottom = NativeMindsTheme.spacing.xl,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(NativeMindsTheme.sizes.unlockMedallion)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Lock(
                tint = MaterialTheme.colorScheme.onPrimary,
                size = MEDALLION_GLYPH_SIZE_DP.dp,
            )
        }

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

        Text(
            text = stringResource(R.string.reader_unlock_title),
            style = NativeMindsTheme.typography.unlockTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.sm))

        Text(
            text = stringResource(R.string.reader_unlock_body, freeSharePercent),
            style = NativeMindsTheme.typography.unlockBody,
            color = NativeMindsTheme.colors.textMuted,
        )

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md)) {
            Benefit(labelRes = R.string.reader_unlock_benefit_library)
            Benefit(labelRes = R.string.reader_unlock_benefit_audio)
            Benefit(labelRes = R.string.reader_unlock_benefit_ai)
        }

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.xl))

        Text(
            text = stringResource(R.string.reader_unlock_action),
            style = NativeMindsTheme.typography.actionLabel,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(NativeMindsTheme.sizes.actionButton)
                .clip(Pill)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onSubscribe)
                .padding(vertical = NativeMindsTheme.spacing.md),
        )

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.md))

        Text(
            text = stringResource(R.string.reader_unlock_terms),
            style = NativeMindsTheme.typography.unlockTerms,
            color = NativeMindsTheme.colors.textSubtle,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Benefit(@StringRes labelRes: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(NativeMindsTheme.sizes.benefitCheck)
                .clip(CircleShape)
                .background(NativeMindsTheme.colors.benefitCheckBackground),
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Check(tint = NativeMindsTheme.colors.benefitCheckContent)
        }
        Text(
            text = stringResource(labelRes),
            style = NativeMindsTheme.typography.unlockBenefit,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@ThemePreviews
@Composable
private fun PremiumUnlockContentPreview() {
    PreviewSurface {
        PremiumUnlockContent(freeSharePercent = 30, onSubscribe = {})
    }
}
