package com.example.nativeminds.feature.paywall.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.nativeminds.feature.paywall.ui.paywall.PaywallScreen
import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan

fun NavGraphBuilder.paywallScreen(
    onClose: () -> Unit,
    onPurchased: (storyId: Long, progressPercent: Int, plan: PurchasePlan) -> Unit,
) {
    composable<PaywallRoute> {
        PaywallScreen(onClose = onClose, onPurchased = onPurchased)
    }
}
