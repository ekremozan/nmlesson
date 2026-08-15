package com.example.nativeminds.feature.settings.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.nativeminds.feature.settings.ui.SettingsScreen

fun NavGraphBuilder.settingsScreen(onBack: () -> Unit, onPremiumClick: () -> Unit) {
    composable<SettingsRoute> {
        SettingsScreen(
            onBack = onBack,
            onPremiumClick = onPremiumClick,
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        )
    }
}
