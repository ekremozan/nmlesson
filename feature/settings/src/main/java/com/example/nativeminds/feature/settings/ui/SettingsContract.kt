package com.example.nativeminds.feature.settings.ui

data class SettingsUiState(
    val isDarkTheme: Boolean = false,
)

sealed interface SettingsIntent {
    data object ThemeToggleClicked : SettingsIntent
    data class ThemeChanged(val isDarkTheme: Boolean) : SettingsIntent
    data object PremiumClicked : SettingsIntent
}

sealed interface SettingsEffect {
    data object NavigateToPaywall : SettingsEffect
    data class PersistDarkTheme(val isDarkTheme: Boolean) : SettingsEffect
}

data class SettingsReduction(
    val state: SettingsUiState,
    val effects: List<SettingsEffect> = emptyList(),
)
