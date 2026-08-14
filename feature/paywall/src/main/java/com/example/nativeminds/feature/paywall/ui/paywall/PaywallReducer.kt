package com.example.nativeminds.feature.paywall.ui.paywall

/**
 * The only thing in the paywall screen that writes state.
 *
 * Pure and top-level, same shape as `:feature:reader`'s reducer: no Android, no coroutines, no
 * ViewModel, so plan selection and the inert restore affordance are both provable in a plain JVM
 * test.
 */
fun PaywallUiState.reduce(intent: PaywallIntent): PaywallReduction = when (intent) {
    is PaywallIntent.PlanSelected -> PaywallReduction(copy(selectedPlan = intent.plan))

    PaywallIntent.PurchaseClicked -> PaywallReduction(
        this,
        listOf(PaywallEffect.Purchased(storyId, progressPercent, selectedPlan)),
    )

    PaywallIntent.RestorePurchasesClicked -> PaywallReduction(
        this,
        listOf(PaywallEffect.ShowNoPurchaseFound),
    )
}
