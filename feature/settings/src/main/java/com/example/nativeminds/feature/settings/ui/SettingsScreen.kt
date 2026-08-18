package com.example.nativeminds.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.example.nativeminds.designsystem.icons.NativeMindsIcons
import com.example.nativeminds.designsystem.preview.ScreenThemePreviews
import com.example.nativeminds.designsystem.theme.NativeMindsTheme
import com.example.nativeminds.designsystem.theme.Pill
import com.example.nativeminds.feature.settings.BuildConfig
import com.example.nativeminds.feature.settings.R
import com.example.nativeminds.feature.settings.ui.preview.SettingsPreviewCase
import com.example.nativeminds.feature.settings.ui.preview.SettingsPreviewCases

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onPremiumClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(viewModel, lifecycle) {
        viewModel.effects.flowWithLifecycle(lifecycle).collect { effect ->
            when (effect) {
                SettingsEffect.NavigateToPaywall -> onPremiumClick()
                is SettingsEffect.PersistDarkTheme -> Unit
                SettingsEffect.CancelPremium -> Unit
            }
        }
    }

    SettingsScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onTestCrashClick = { throw RuntimeException("Test crash") },
        modifier = modifier,
    )
}

/**
 * Stateless so it's directly usable from `@Preview`s. [onTestCrashClick] is a plain callback, not
 * an intent, the same way navigating out of a screen is: it changes no state, it only proves to a
 * QA build that a real crash reaches Crashlytics (spec 005, US2/SC-002).
 */
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onTestCrashClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = NativeMindsTheme.spacing.screen),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier.padding(top = NativeMindsTheme.spacing.xs)) {
                BackButton(onClick = onBack)
            }

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.xl))

            DarkThemeRow(
                isDarkTheme = state.isDarkTheme,
                onToggle = { onIntent(SettingsIntent.ThemeToggleClicked) },
            )

            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.md))

            PremiumCard(
                isPremium = state.isPremium,
                onUpgradeClick = { onIntent(SettingsIntent.PremiumClicked) },
                onCancelClick = { onIntent(SettingsIntent.CancelPremiumClicked) },
            )

            if (state.showCancelPremiumDialog) {
                CancelPremiumDialog(
                    onConfirm = { onIntent(SettingsIntent.CancelPremiumConfirmed) },
                    onDismiss = { onIntent(SettingsIntent.CancelPremiumDialogDismissed) },
                )
            }
        }

        if (BuildConfig.DEBUG) {
            SectionHeading(text = stringResource(R.string.settings_section_debug))
            SettingsCard {
                DebugTestCrashRow(onClick = onTestCrashClick)
            }
        }

        Text(
            text = stringResource(R.string.settings_app_version),
            style = MaterialTheme.typography.bodySmall,
            color = NativeMindsTheme.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = NativeMindsTheme.spacing.xl),
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(NativeMindsTheme.sizes.iconButton)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NativeMindsIcons.ChevronLeft(tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DarkThemeRow(isDarkTheme: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, NativeMindsTheme.colors.cardBorder, MaterialTheme.shapes.large)
            .padding(NativeMindsTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(NativeMindsTheme.sizes.benefitIcon)
                .clip(CircleShape)
                .background(NativeMindsTheme.colors.premiumChipBackground),
            contentAlignment = Alignment.Center,
        ) {
            NativeMindsIcons.Moon(tint = NativeMindsTheme.colors.premiumChipContent)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_dark_theme_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    if (isDarkTheme) {
                        R.string.settings_dark_theme_subtitle_on
                    } else {
                        R.string.settings_dark_theme_subtitle_off
                    },
                ),
                style = NativeMindsTheme.typography.lessonTeaser,
                color = NativeMindsTheme.colors.textMuted,
            )
        }
        Switch(
            checked = isDarkTheme,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}

@Composable
private fun PremiumCard(isPremium: Boolean, onUpgradeClick: () -> Unit, onCancelClick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(NativeMindsTheme.colors.premiumChipBackground)
            .padding(NativeMindsTheme.spacing.lg),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(NativeMindsTheme.sizes.benefitIcon)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                NativeMindsIcons.Sparkle(tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (isPremium) {
                            R.string.settings_premium_active_title
                        } else {
                            R.string.settings_premium_title
                        },
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = NativeMindsTheme.colors.premiumChipContent,
                )
                Text(
                    text = stringResource(
                        if (isPremium) {
                            R.string.settings_premium_active_body
                        } else {
                            R.string.settings_premium_body
                        },
                    ),
                    style = NativeMindsTheme.typography.lessonTeaser,
                    color = NativeMindsTheme.colors.premiumChipContent,
                )
            }
        }
        Text(
            text = stringResource(
                if (isPremium) R.string.settings_premium_cancel_cta else R.string.settings_premium_cta,
            ),
            style = NativeMindsTheme.typography.actionLabel,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(NativeMindsTheme.sizes.actionButton)
                .clip(Pill)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = if (isPremium) onCancelClick else onUpgradeClick)
                .padding(vertical = NativeMindsTheme.spacing.md),
        )
    }
}

@Composable
private fun CancelPremiumDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = NativeMindsTheme.colors.textMuted,
        title = {
            Text(
                text = stringResource(R.string.settings_cancel_premium_dialog_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.settings_cancel_premium_dialog_body),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.settings_cancel_premium_dialog_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.settings_cancel_premium_dialog_dismiss),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text.uppercase(),
        style = NativeMindsTheme.typography.sectionHeading,
        color = NativeMindsTheme.colors.textMuted,
        modifier = Modifier.padding(vertical = NativeMindsTheme.spacing.md),
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, NativeMindsTheme.colors.cardBorder, MaterialTheme.shapes.large),
        content = content,
    )
}

@Composable
private fun DebugTestCrashRow(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(NativeMindsTheme.spacing.md),
    ) {
        Text(
            text = stringResource(R.string.settings_test_crash),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
        )
    }
}

@ScreenThemePreviews
@Composable
private fun SettingsScreenContentPreview(
    @PreviewParameter(SettingsPreviewCases::class) case: SettingsPreviewCase,
) {
    NativeMindsTheme {
        SettingsScreenContent(
            state = case.state,
            onIntent = {},
            onBack = {},
            onTestCrashClick = {},
        )
    }
}
