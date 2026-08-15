package com.example.nativeminds.feature.paywall.ui.paywall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.example.nativeminds.domain.observability.AnalyticsEvent
import com.example.nativeminds.domain.observability.AnalyticsReporter
import com.example.nativeminds.domain.repository.EntitlementRepository
import com.example.nativeminds.feature.paywall.navigation.PaywallRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class PaywallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val entitlementRepository: EntitlementRepository,
    private val analyticsReporter: AnalyticsReporter,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<PaywallRoute>()

    private val _state = MutableStateFlow(
        PaywallUiState(lessonId = route.lessonId, progressPercent = route.progressPercent),
    )

    val state: StateFlow<PaywallUiState> = _state.asStateFlow()

    private val effectChannel = Channel<PaywallEffect>(Channel.BUFFERED)

    val effects: Flow<PaywallEffect> = effectChannel.receiveAsFlow()

    /** Whether [PaywallIntent.PurchaseClicked] has already been handled this screen visit. */
    private var purchased = false

    init {
        analyticsReporter.log(AnalyticsEvent.PaywallShown(route.lessonId, route.triggerSource))
    }

    /**
     * The one place the mock purchase is granted: keyed to the one intent that means "buy now,"
     * with no branch on what the intent *means* — the reducer already decided that. This is the
     * effectful counterpart to the pure reducer, which cannot itself call a repository.
     */
    fun onIntent(intent: PaywallIntent) {
        val reduction = _state.value.reduce(intent)
        _state.value = reduction.state
        if (intent is PaywallIntent.PurchaseClicked) {
            val plan = reduction.state.selectedPlan.name
            analyticsReporter.log(AnalyticsEvent.PaywallPurchaseClicked(route.lessonId, plan))
            entitlementRepository.setPremium(true)
            analyticsReporter.log(AnalyticsEvent.SubscriptionStarted(route.lessonId, plan))
            purchased = true
        }
        if (intent is PaywallIntent.RestorePurchasesClicked) {
            analyticsReporter.log(AnalyticsEvent.RestorePurchasesClicked(route.lessonId))
        }
        reduction.effects.forEach(effectChannel::trySend)
    }

    /**
     * Leaving without having purchased is exactly what "dismissed the paywall" means — the
     * purchased path pops this screen too (see `onPurchased` in the nav host), so [purchased]
     * is what tells the two apart here rather than a second, forbidden intent for navigating out.
     */
    override fun onCleared() {
        if (!purchased) {
            analyticsReporter.log(AnalyticsEvent.PaywallDismissed(route.lessonId))
        }
    }
}
