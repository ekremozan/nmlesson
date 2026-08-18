package com.example.nativeminds.feature.settings.ui

fun SettingsUiState.reduce(intent: SettingsIntent): SettingsReduction = when (intent) {
    SettingsIntent.ThemeToggleClicked -> {
        val newState = copy(isDarkTheme = !isDarkTheme)
        SettingsReduction(newState, listOf(SettingsEffect.PersistDarkTheme(newState.isDarkTheme)))
    }
    is SettingsIntent.ThemeChanged -> SettingsReduction(copy(isDarkTheme = intent.isDarkTheme))
    SettingsIntent.PremiumClicked -> SettingsReduction(this, listOf(SettingsEffect.NavigateToPaywall))
    is SettingsIntent.PremiumStatusChanged -> SettingsReduction(copy(isPremium = intent.isPremium))
    SettingsIntent.CancelPremiumClicked -> SettingsReduction(copy(showCancelPremiumDialog = true))
    SettingsIntent.CancelPremiumDialogDismissed -> SettingsReduction(copy(showCancelPremiumDialog = false))
    SettingsIntent.CancelPremiumConfirmed ->
        SettingsReduction(copy(showCancelPremiumDialog = false), listOf(SettingsEffect.CancelPremium))
}
