package com.example.nativeminds.feature.paywall.ui.success

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.ScreenThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.paywall.R
import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan
import com.example.nativeminds.feature.paywall.ui.preview.PurchaseSuccessPreviewCase
import com.example.nativeminds.feature.paywall.ui.preview.PurchaseSuccessPreviewCases

private const val PERCENT = 100

@Composable
fun PurchaseSuccessScreen(
    onContinueReading: (storyId: Long) -> Unit,
    onExploreLibrary: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PurchaseSuccessViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PurchaseSuccessScreenContent(
        state = state,
        onContinueReading = { onContinueReading(state.storyId) },
        onExploreLibrary = onExploreLibrary,
        modifier = modifier,
    )
}

@Composable
fun PurchaseSuccessScreenContent(
    state: PurchaseSuccessUiState,
    onContinueReading: () -> Unit,
    onExploreLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clipToBounds(),
    ) {
        SuccessBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = NativeMindsTheme.spacing.screen,
                    vertical = NativeMindsTheme.spacing.xl,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(NativeMindsTheme.sizes.benefitIcon)
                    .clip(CircleShape)
                    .background(NativeMindsTheme.colors.benefitCheckBackground),
                contentAlignment = Alignment.Center,
            ) {
                NativeMindsIcons.Check(
                    tint = NativeMindsTheme.colors.benefitCheckContent,
                    size = 20.dp,
                )
            }

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

            Text(
                text = stringResource(R.string.paywall_success_headline),
                style = NativeMindsTheme.typography.unlockTitle,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.sm))

            Text(
                text = stringResource(
                    R.string.paywall_success_body,
                    stringResource(
                        if (state.plan == PurchasePlan.MONTHLY) {
                            R.string.paywall_success_plan_monthly
                        } else {
                            R.string.paywall_success_plan_yearly
                        },
                    ),
                ),
                style = NativeMindsTheme.typography.unlockBody,
                color = NativeMindsTheme.colors.textMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(NativeMindsTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.sm),
            ) {
                RecapRow(R.string.paywall_success_recap_library)
                RecapRow(R.string.paywall_success_recap_audio)
                RecapRow(R.string.paywall_success_recap_ai)
            }

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.md))

            val story = state.story
            if (story != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(NativeMindsTheme.colors.premiumChipBackground)
                        .padding(NativeMindsTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.paywall_success_resume_label),
                            style = NativeMindsTheme.typography.premiumBadge,
                            color = NativeMindsTheme.colors.premiumChipContent,
                        )
                        Text(
                            text = story.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.paywall_success_resume_progress,
                                state.progressPercent,
                                remainingMinutes(story.minutes, state.progressPercent),
                            ),
                            style = NativeMindsTheme.typography.progressLabel,
                            color = NativeMindsTheme.colors.premiumChipContent,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.md))
            }

            Text(
                text = stringResource(R.string.paywall_success_continue_reading),
                style = NativeMindsTheme.typography.actionLabel,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NativeMindsTheme.sizes.actionButton)
                    .clip(Pill)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onContinueReading)
                    .padding(vertical = NativeMindsTheme.spacing.md),
            )

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.sm))

            Text(
                text = stringResource(R.string.paywall_success_explore_library),
                style = NativeMindsTheme.typography.unlockBenefit,
                color = NativeMindsTheme.colors.accentText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExploreLibrary)
                    .padding(vertical = NativeMindsTheme.spacing.sm),
            )

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.md))

            Text(
                text = stringResource(R.string.paywall_success_receipt_note),
                style = NativeMindsTheme.typography.unlockTerms,
                color = NativeMindsTheme.colors.textSubtle,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val SUCCESS_CIRCLE_SAGE_SIZE_DP = 230
private const val SUCCESS_CIRCLE_SAGE_X_DP = -56
private const val SUCCESS_CIRCLE_SAGE_Y_DP = -72
private const val SUCCESS_CIRCLE_SAGE_ALPHA = 0.24f
private const val SUCCESS_CIRCLE_ACCENT_SIZE_DP = 170
private const val SUCCESS_CIRCLE_ACCENT_X_DP = 64
private const val SUCCESS_CIRCLE_ACCENT_Y_DP = 120
private const val SUCCESS_CIRCLE_ACCENT_ALPHA = 0.18f

/**
 * Two soft blobs behind the confirmation, drawn outside the content column's insets so they run
 * behind the status bar the way the design does. No band and no covers here — the paywall earns
 * that weight because it has to sell something; this screen has already been paid.
 */
@Composable
private fun SuccessBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = SUCCESS_CIRCLE_SAGE_X_DP.dp, y = SUCCESS_CIRCLE_SAGE_Y_DP.dp)
                .size(SUCCESS_CIRCLE_SAGE_SIZE_DP.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = SUCCESS_CIRCLE_SAGE_ALPHA),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = SUCCESS_CIRCLE_ACCENT_X_DP.dp, y = SUCCESS_CIRCLE_ACCENT_Y_DP.dp)
                .size(SUCCESS_CIRCLE_ACCENT_SIZE_DP.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = SUCCESS_CIRCLE_ACCENT_ALPHA),
                ),
        )
    }
}

private const val RECAP_CHECK_SIZE_DP = 20

@Composable
private fun RecapRow(@StringRes titleRes: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(NativeMindsTheme.sizes.benefitIcon)
                .clip(CircleShape)
                .background(NativeMindsTheme.colors.benefitCheckBackground),
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Check(
                tint = NativeMindsTheme.colors.benefitCheckContent,
                size = RECAP_CHECK_SIZE_DP.dp,
            )
        }
        Text(
            text = stringResource(titleRes),
            style = NativeMindsTheme.typography.unlockBenefit,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun remainingMinutes(totalMinutes: Int, progressPercent: Int): Int =
    (totalMinutes * (PERCENT - progressPercent)) / PERCENT

@ScreenThemePreviews
@Composable
private fun PurchaseSuccessScreenContentPreview(
    @PreviewParameter(PurchaseSuccessPreviewCases::class) case: PurchaseSuccessPreviewCase,
) {
    NativeMindsTheme {
        PurchaseSuccessScreenContent(
            state = case.state,
            onContinueReading = {},
            onExploreLibrary = {},
        )
    }
}
