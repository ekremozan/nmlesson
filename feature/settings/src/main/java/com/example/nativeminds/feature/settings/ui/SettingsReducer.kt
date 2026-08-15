package com.example.nativeminds.feature.settings.ui

fun SettingsUiState.reduce(intent: SettingsIntent): SettingsReduction = when (intent) {
    SettingsIntent.ThemeToggleClicked -> SettingsReduction(copy(isDarkTheme = !isDarkTheme))
    is SettingsIntent.ThemeChanged -> SettingsReduction(copy(isDarkTheme = intent.isDarkTheme))
    SettingsIntent.PremiumClicked -> SettingsReduction(this, listOf(SettingsEffect.NavigateToPaywall))
}
