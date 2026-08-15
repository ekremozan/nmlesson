package com.example.nativeminds.feature.paywall.ui.paywall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter
import com.example.nativeminds.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROBOLECTRIC_SDK = 36
private const val LESSON_ID = 3L
private const val PROGRESS_PERCENT = 30

private class TestEntitlementRepository : EntitlementRepository {
    val flow = MutableStateFlow(false)

    override fun isPremium(): Flow<Boolean> = flow

    override fun setPremium(value: Boolean) {
        flow.value = value
    }
}

private class RecordingAnalyticsReporter : AnalyticsReporter {
    val logged = mutableListOf<AnalyticsEvent>()

    override fun log(event: AnalyticsEvent) {
        logged += event
    }
}

/** Robolectric for the same reason as `ReaderViewModelTest`: a real `Bundle` behind the route. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ROBOLECTRIC_SDK])
class PaywallViewModelTest {
    private fun viewModel(
        entitlementRepository: EntitlementRepository = TestEntitlementRepository(),
        analyticsReporter: RecordingAnalyticsReporter = RecordingAnalyticsReporter(),
        triggerSource: String = "reader_unlock",
    ) = PaywallViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                "lessonId" to LESSON_ID,
                "progressPercent" to PROGRESS_PERCENT,
                "triggerSource" to triggerSource,
            ),
        ),
        entitlementRepository = entitlementRepository,
        analyticsReporter = analyticsReporter,
    )

    @Test
    fun creatingTheViewModelLogsExactlyOnePaywallShown() {
        val analyticsReporter = RecordingAnalyticsReporter()

        viewModel(analyticsReporter = analyticsReporter, triggerSource = "settings_premium")

        assertEquals(
            listOf(AnalyticsEvent.PaywallShown(LESSON_ID, "settings_premium")),
            analyticsReporter.logged,
        )
    }

    @Test
    fun purchaseClickedLogsPurchaseClickedThenSubscriptionStarted() {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = viewModel(analyticsReporter = analyticsReporter)

        viewModel.onIntent(PaywallIntent.PurchaseClicked)

        assertEquals(
            listOf(
                AnalyticsEvent.PaywallShown(LESSON_ID, "reader_unlock"),
                AnalyticsEvent.PaywallPurchaseClicked(LESSON_ID, PurchasePlan.MONTHLY.name),
                AnalyticsEvent.SubscriptionStarted(LESSON_ID, PurchasePlan.MONTHLY.name),
            ),
            analyticsReporter.logged,
        )
    }

    @Test
    fun clearingWithoutPurchasingLogsPaywallDismissed() {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = viewModel(analyticsReporter = analyticsReporter)

        val store = ViewModelStore()
        store.put("paywall", viewModel)
        store.clear()

        assertTrue(analyticsReporter.logged.contains(AnalyticsEvent.PaywallDismissed(LESSON_ID)))
    }

    @Test
    fun restorePurchasesClickedLogsRestorePurchasesClicked() {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = viewModel(analyticsReporter = analyticsReporter)

        viewModel.onIntent(PaywallIntent.RestorePurchasesClicked)

        assertTrue(analyticsReporter.logged.contains(AnalyticsEvent.RestorePurchasesClicked(LESSON_ID)))
    }

    @Test
    fun clearingAfterPurchasingDoesNotLogPaywallDismissed() {
        val analyticsReporter = RecordingAnalyticsReporter()
        val viewModel = viewModel(analyticsReporter = analyticsReporter)
        viewModel.onIntent(PaywallIntent.PurchaseClicked)

        val store = ViewModelStore()
        store.put("paywall", viewModel)
        store.clear()

        assertTrue(analyticsReporter.logged.none { it is AnalyticsEvent.PaywallDismissed })
    }
}
