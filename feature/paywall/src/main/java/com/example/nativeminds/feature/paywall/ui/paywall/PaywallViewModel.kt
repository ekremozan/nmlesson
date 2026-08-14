package com.example.nativeminds.feature.paywall.ui.paywall

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
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
) : ViewModel() {
    private val route = savedStateHandle.toRoute<PaywallRoute>()

    private val _state = MutableStateFlow(
        PaywallUiState(lessonId = route.lessonId, progressPercent = route.progressPercent),
    )

    val state: StateFlow<PaywallUiState> = _state.asStateFlow()

    private val effectChannel = Channel<PaywallEffect>(Channel.BUFFERED)

    val effects: Flow<PaywallEffect> = effectChannel.receiveAsFlow()

    /**
     * The one place the mock purchase is granted: keyed to the one intent that means "buy now,"
     * with no branch on what the intent *means* — the reducer already decided that. This is the
     * effectful counterpart to the pure reducer, which cannot itself call a repository.
     */
    fun onIntent(intent: PaywallIntent) {
        val reduction = _state.value.reduce(intent)
        _state.value = reduction.state
        if (intent is PaywallIntent.PurchaseClicked) {
            entitlementRepository.setPremium(true)
        }
        reduction.effects.forEach(effectChannel::trySend)
    }
}
