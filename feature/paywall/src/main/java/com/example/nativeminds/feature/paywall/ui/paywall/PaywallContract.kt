package com.example.nativeminds.feature.paywall.ui.paywall

/** The paywall's state — irreducible facts only: which plan is currently highlighted. */
data class PaywallUiState(
    val storyId: Long,
    val progressPercent: Int,
    val selectedPlan: PurchasePlan = PurchasePlan.MONTHLY,
)

sealed interface PaywallIntent {
    data class PlanSelected(val plan: PurchasePlan) : PaywallIntent

    data object PurchaseClicked : PaywallIntent

    data object RestorePurchasesClicked : PaywallIntent
}

sealed interface PaywallEffect {
    data class Purchased(val storyId: Long, val progressPercent: Int, val plan: PurchasePlan) :
        PaywallEffect

    data object ShowNoPurchaseFound : PaywallEffect
}

data class PaywallReduction(
    val state: PaywallUiState,
    val effects: List<PaywallEffect> = emptyList(),
)
