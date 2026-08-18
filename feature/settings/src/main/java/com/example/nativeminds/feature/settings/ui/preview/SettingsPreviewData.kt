package com.example.nativeminds.feature.settings.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.nativeminds.feature.settings.ui.SettingsUiState

/** One settings state, labelled so a preview row says what it is showing. */
data class SettingsPreviewCase(val label: String, val state: SettingsUiState)

class SettingsPreviewCases : PreviewParameterProvider<SettingsPreviewCase> {
    override val values = sequenceOf(
        SettingsPreviewCase(label = "Light theme selected", state = SettingsUiState(isDarkTheme = false)),
        SettingsPreviewCase(label = "Dark theme selected", state = SettingsUiState(isDarkTheme = true)),
        SettingsPreviewCase(label = "Premium active", state = SettingsUiState(isPremium = true)),
        SettingsPreviewCase(
            label = "Cancel premium dialog",
            state = SettingsUiState(isPremium = true, showCancelPremiumDialog = true),
        ),
    )
}
