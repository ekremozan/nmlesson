package com.example.nativeminds.feature.paywall.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.nativeminds.feature.paywall.ui.success.PurchaseSuccessScreen

fun NavGraphBuilder.purchaseSuccessScreen(
    onContinueReading: (storyId: Long) -> Unit,
    onExploreLibrary: () -> Unit,
) {
    composable<PurchaseSuccessRoute> {
        PurchaseSuccessScreen(
            onContinueReading = onContinueReading,
            onExploreLibrary = onExploreLibrary,
        )
    }
}
