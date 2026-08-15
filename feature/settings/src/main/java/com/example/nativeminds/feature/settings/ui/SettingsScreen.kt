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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NativeMindsTheme.spacing.screen),
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

        PremiumCard(onClick = { onIntent(SettingsIntent.PremiumClicked) })

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.xl))

        SectionHeading(text = stringResource(R.string.settings_section_support))
        SettingsCard {
            SettingsRow(
                icon = NativeMindsIcons::Star,
                label = stringResource(R.string.settings_rate_us),
            )
            HorizontalDivider(color = NativeMindsTheme.colors.cardBorder)
            SettingsRow(
                icon = NativeMindsIcons::Mail,
                label = stringResource(R.string.settings_contact_us),
            )
        }

        Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))

        SectionHeading(text = stringResource(R.string.settings_section_legal))
        SettingsCard {
            SettingsRow(
                icon = NativeMindsIcons::Shield,
                label = stringResource(R.string.settings_privacy_policy),
            )
            HorizontalDivider(color = NativeMindsTheme.colors.cardBorder)
            SettingsRow(
                icon = NativeMindsIcons::Document,
                label = stringResource(R.string.settings_terms_of_use),
            )
        }

        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(NativeMindsTheme.spacing.lg))
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
private fun PremiumCard(onClick: () -> Unit) {
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
                    text = stringResource(R.string.settings_premium_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = NativeMindsTheme.colors.premiumChipContent,
                )
                Text(
                    text = stringResource(R.string.settings_premium_body),
                    style = NativeMindsTheme.typography.lessonTeaser,
                    color = NativeMindsTheme.colors.premiumChipContent,
                )
            }
        }
        Text(
            text = stringResource(R.string.settings_premium_cta),
            style = NativeMindsTheme.typography.actionLabel,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(NativeMindsTheme.sizes.actionButton)
                .clip(Pill)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onClick)
                .padding(vertical = NativeMindsTheme.spacing.md),
        )
    }
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

/** A row with no [onClick]: rate/contact/legal destinations aren't wired up yet. */
@Composable
private fun SettingsRow(
    icon: @Composable (tint: androidx.compose.ui.graphics.Color, modifier: Modifier, size: androidx.compose.ui.unit.Dp) -> Unit,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NativeMindsTheme.spacing.md),
        modifier = Modifier
            .fillMaxWidth()
            .padding(NativeMindsTheme.spacing.md),
    ) {
        icon(NativeMindsTheme.colors.accentText, Modifier, 19.dp)
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        NativeMindsIcons.ChevronRight(tint = NativeMindsTheme.colors.textSubtle)
    }
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
