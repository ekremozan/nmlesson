package com.example.nativeminds.feature.paywall.ui.paywall

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private fun initialState() = PaywallUiState(lessonId = 3, progressPercent = 30)

class PaywallReducerTest {
    @Test
    fun defaultsToTheMonthlyPlan() {
        assertEquals(PurchasePlan.MONTHLY, initialState().selectedPlan)
    }

    @Test
    fun selectingYearlyChangesOnlyTheSelection() {
        val reduction = initialState().reduce(PaywallIntent.PlanSelected(PurchasePlan.YEARLY))

        assertEquals(PurchasePlan.YEARLY, reduction.state.selectedPlan)
        assertTrue(reduction.effects.isEmpty())
    }

    @Test
    fun selectingMonthlyAfterYearlySwitchesBack() {
        val yearly = initialState().reduce(PaywallIntent.PlanSelected(PurchasePlan.YEARLY)).state

        val reduction = yearly.reduce(PaywallIntent.PlanSelected(PurchasePlan.MONTHLY))

        assertEquals(PurchasePlan.MONTHLY, reduction.state.selectedPlan)
    }

    @Test
    fun purchasingRaisesAnEffectCarryingTheSelectedPlanAndChangesNoState() {
        val state = initialState().reduce(PaywallIntent.PlanSelected(PurchasePlan.YEARLY)).state

        val reduction = state.reduce(PaywallIntent.PurchaseClicked)

        assertEquals(state, reduction.state)
        assertEquals(
            listOf(PaywallEffect.Purchased(state.lessonId, state.progressPercent, PurchasePlan.YEARLY)),
            reduction.effects,
        )
    }

    @Test
    fun restoringPurchasesRaisesAnEffectAndChangesNoState() {
        val state = initialState()

        val reduction = state.reduce(PaywallIntent.RestorePurchasesClicked)

        assertEquals(state, reduction.state)
        assertEquals(listOf(PaywallEffect.ShowNoPurchaseFound), reduction.effects)
    }
}
