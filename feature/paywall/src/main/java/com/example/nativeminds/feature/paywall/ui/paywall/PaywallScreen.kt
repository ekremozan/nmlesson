package com.example.nativeminds.feature.paywall.ui.paywall

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.ScreenThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.paywall.R
import com.example.nativeminds.feature.paywall.ui.preview.PaywallPreviewCase
import com.example.nativeminds.feature.paywall.ui.preview.PaywallPreviewCases

@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    onPurchased: (lessonId: Long, progressPercent: Int, plan: PurchasePlan) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val noPurchaseFoundMessage = stringResource(R.string.paywall_restore_purchases_result)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel, lifecycle) {
        viewModel.effects.flowWithLifecycle(lifecycle).collect { effect ->
            when (effect) {
                is PaywallEffect.Purchased ->
                    onPurchased(effect.lessonId, effect.progressPercent, effect.plan)

                PaywallEffect.ShowNoPurchaseFound ->
                    snackbarHostState.showSnackbar(noPurchaseFoundMessage)
            }
        }
    }

    PaywallScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onClose = onClose,
        modifier = modifier,
    )
}

/** Stateless so it is directly usable from previews. */
@Composable
fun PaywallScreenContent(
    state: PaywallUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (PaywallIntent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        HeroBackdrop(modifier = Modifier.align(Alignment.TopCenter))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = NativeMindsTheme.spacing.screen),
            ) {
                Box(modifier = Modifier.padding(top = NativeMindsTheme.spacing.xs)) {
                    CloseButton(onClick = onClose)
                }

                Spacer(modifier = Modifier.height(HERO_CONTENT_OFFSET_DP.dp))

                Text(
                    text = stringResource(R.string.paywall_headline),
                    style = NativeMindsTheme.typography.unlockTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.sm))

                Text(
                    text = stringResource(R.string.paywall_body),
                    style = NativeMindsTheme.typography.unlockBody,
                    color = NativeMindsTheme.colors.textMuted,
                )

                Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

                Column(verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md)) {
                    BenefitRow(
                        icon = NativeMindsIcons::Library,
                        peach = true,
                        titleRes = R.string.paywall_benefit_unlimited_title,
                        bodyRes = R.string.paywall_benefit_unlimited_body,
                    )
                    BenefitRow(
                        icon = NativeMindsIcons::Headphones,
                        peach = false,
                        titleRes = R.string.paywall_benefit_audio_title,
                        bodyRes = R.string.paywall_benefit_audio_body,
                    )
                    BenefitRow(
                        icon = NativeMindsIcons::Sparkle,
                        peach = true,
                        titleRes = R.string.paywall_benefit_ai_title,
                        bodyRes = R.string.paywall_benefit_ai_body,
                    )
                    BenefitRow(
                        icon = NativeMindsIcons::Download,
                        peach = false,
                        titleRes = R.string.paywall_benefit_offline_title,
                        bodyRes = R.string.paywall_benefit_offline_body,
                    )
                }

                Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))
            }

            Column(
                modifier = Modifier.padding(
                    start = NativeMindsTheme.spacing.screen,
                    end = NativeMindsTheme.spacing.screen,
                    bottom = NativeMindsTheme.spacing.md,
                ),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md)) {
                    PlanCard(
                        selected = state.selectedPlan == PurchasePlan.MONTHLY,
                        labelRes = R.string.paywall_plan_monthly_label,
                        priceRes = R.string.paywall_plan_monthly_price,
                        terms = stringResource(R.string.paywall_plan_monthly_period),
                        badgeRes = null,
                        onClick = { onIntent(PaywallIntent.PlanSelected(PurchasePlan.MONTHLY)) },
                        modifier = Modifier.weight(1f),
                    )
                    PlanCard(
                        selected = state.selectedPlan == PurchasePlan.YEARLY,
                        labelRes = R.string.paywall_plan_yearly_label,
                        priceRes = R.string.paywall_plan_yearly_price,
                        terms = stringResource(R.string.paywall_plan_yearly_terms),
                        badgeRes = R.string.paywall_plan_yearly_badge,
                        onClick = { onIntent(PaywallIntent.PlanSelected(PurchasePlan.YEARLY)) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

                Text(
                    text = stringResource(R.string.paywall_cta_label),
                    style = NativeMindsTheme.typography.actionLabel,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(NativeMindsTheme.sizes.actionButton)
                        .clip(Pill)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onIntent(PaywallIntent.PurchaseClicked) }
                        .padding(vertical = NativeMindsTheme.spacing.md),
                )

                Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.sm))

                Text(
                    text = stringResource(
                        if (state.selectedPlan == PurchasePlan.MONTHLY) {
                            R.string.paywall_cta_sub_monthly
                        } else {
                            R.string.paywall_cta_sub_yearly
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = NativeMindsTheme.colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.paywall_restore_purchases),
                        style = NativeMindsTheme.typography.unlockTerms,
                        color = NativeMindsTheme.colors.textSubtle,
                        modifier = Modifier.clickable {
                            onIntent(PaywallIntent.RestorePurchasesClicked)
                        },
                    )
                    Spacer(modifier = Modifier.width(NativeMindsTheme.spacing.md))
                    Text(
                        text = stringResource(R.string.paywall_terms),
                        style = NativeMindsTheme.typography.unlockTerms,
                        color = NativeMindsTheme.colors.textSubtle,
                    )
                    Spacer(modifier = Modifier.width(NativeMindsTheme.spacing.md))
                    Text(
                        text = stringResource(R.string.paywall_privacy),
                        style = NativeMindsTheme.typography.unlockTerms,
                        color = NativeMindsTheme.colors.textSubtle,
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * Distance from the close button to the headline, which is what drops the headline just below the
 * hero band. Together with the status-bar inset and the close button's own height it reproduces
 * the design's 280dp headline offset from the top of the screen.
 */
private const val HERO_CONTENT_OFFSET_DP = 186

private const val CLOSE_BUTTON_ALPHA = 0.82f

/** Slightly translucent so the hero it sits on stays readable through it, as the design draws it. */
@Composable
private fun CloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(NativeMindsTheme.sizes.iconButton)
            .clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = CLOSE_BUTTON_ALPHA),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NativeMindsIcons.Close(tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BenefitRow(
    icon: @Composable (tint: Color, modifier: Modifier, size: Dp) -> Unit,
    peach: Boolean,
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
) {
    val background = if (peach) {
        NativeMindsTheme.colors.premiumChipBackground
    } else {
        NativeMindsTheme.colors.benefitCheckBackground
    }
    val content = if (peach) {
        NativeMindsTheme.colors.premiumChipContent
    } else {
        NativeMindsTheme.colors.benefitCheckContent
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md)) {
        Box(
            modifier = Modifier
                .size(NativeMindsTheme.sizes.benefitIcon)
                .clip(CircleShape)
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            icon(content, Modifier, 20.dp)
        }
        Column {
            Text(
                text = stringResource(titleRes),
                style = NativeMindsTheme.typography.unlockBenefit,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(bodyRes),
                style = NativeMindsTheme.typography.lessonTeaser,
                color = NativeMindsTheme.colors.textMuted,
            )
        }
    }
}

@Composable
private fun PlanCard(
    selected: Boolean,
    @StringRes labelRes: Int,
    @StringRes priceRes: Int,
    terms: String,
    @StringRes badgeRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    },
                )
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        NativeMindsTheme.colors.cardBorder
                    },
                    shape = MaterialTheme.shapes.medium,
                )
                .clickable(onClick = onClick)
                .padding(NativeMindsTheme.spacing.md),
        ) {
            Text(
                text = stringResource(labelRes),
                style = NativeMindsTheme.typography.lessonMeta,
                color = NativeMindsTheme.colors.textMuted,
            )
            Text(
                text = stringResource(priceRes),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = terms,
                style = MaterialTheme.typography.bodySmall,
                color = NativeMindsTheme.colors.textMuted,
            )
        }

        if (badgeRes != null) {
            Text(
                text = stringResource(badgeRes),
                style = NativeMindsTheme.typography.premiumBadge,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = NativeMindsTheme.spacing.sm)
                    .clip(Pill)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = NativeMindsTheme.spacing.sm, vertical = 4.dp),
            )
        }
    }
}

@ScreenThemePreviews
@Composable
private fun PaywallScreenContentPreview(
    @PreviewParameter(PaywallPreviewCases::class) case: PaywallPreviewCase,
) {
    NativeMindsTheme {
        PaywallScreenContent(
            state = case.state,
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {},
            onClose = {},
        )
    }
}
