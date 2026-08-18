package com.example.nativeminds.feature.settings.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsReducerTest {
    @Test
    fun defaultsToLightTheme() {
        assertEquals(false, SettingsUiState().isDarkTheme)
    }

    @Test
    fun togglingTheThemeFlipsItAndRaisesAPersistEffect() {
        val reduction = SettingsUiState(isDarkTheme = false).reduce(SettingsIntent.ThemeToggleClicked)

        assertTrue(reduction.state.isDarkTheme)
        assertEquals(listOf(SettingsEffect.PersistDarkTheme(true)), reduction.effects)
    }

    @Test
    fun togglingTwiceReturnsToTheOriginalValue() {
        val once = SettingsUiState(isDarkTheme = false).reduce(SettingsIntent.ThemeToggleClicked).state

        val reduction = once.reduce(SettingsIntent.ThemeToggleClicked)

        assertEquals(false, reduction.state.isDarkTheme)
    }

    @Test
    fun themeChangedFoldsAnExternalValueIntoState() {
        val reduction = SettingsUiState(isDarkTheme = false).reduce(SettingsIntent.ThemeChanged(true))

        assertTrue(reduction.state.isDarkTheme)
    }

    @Test
    fun premiumClickedRaisesAnEffectAndChangesNoState() {
        val state = SettingsUiState(isDarkTheme = true)

        val reduction = state.reduce(SettingsIntent.PremiumClicked)

        assertEquals(state, reduction.state)
        assertEquals(listOf(SettingsEffect.NavigateToPaywall), reduction.effects)
    }

    @Test
    fun premiumStatusChangedFoldsAnExternalValueIntoState() {
        val reduction = SettingsUiState(isPremium = false).reduce(SettingsIntent.PremiumStatusChanged(true))

        assertTrue(reduction.state.isPremium)
    }

    @Test
    fun cancelPremiumClickedOpensTheConfirmationDialog() {
        val reduction = SettingsUiState(isPremium = true).reduce(SettingsIntent.CancelPremiumClicked)

        assertTrue(reduction.state.showCancelPremiumDialog)
    }

    @Test
    fun cancelPremiumDialogDismissedClosesTheDialogWithNoEffect() {
        val state = SettingsUiState(isPremium = true, showCancelPremiumDialog = true)

        val reduction = state.reduce(SettingsIntent.CancelPremiumDialogDismissed)

        assertEquals(false, reduction.state.showCancelPremiumDialog)
        assertEquals(emptyList<SettingsEffect>(), reduction.effects)
    }

    @Test
    fun cancelPremiumConfirmedClosesTheDialogAndRaisesACancelEffect() {
        val state = SettingsUiState(isPremium = true, showCancelPremiumDialog = true)

        val reduction = state.reduce(SettingsIntent.CancelPremiumConfirmed)

        assertEquals(false, reduction.state.showCancelPremiumDialog)
        assertEquals(listOf(SettingsEffect.CancelPremium), reduction.effects)
    }
}
