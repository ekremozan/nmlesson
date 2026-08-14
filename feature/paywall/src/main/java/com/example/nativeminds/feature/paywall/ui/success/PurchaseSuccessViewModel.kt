package com.example.nativeminds.feature.paywall.ui.success

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.nativeminds.domain.repository.StoryRepository
import com.example.nativeminds.feature.paywall.navigation.PurchaseSuccessRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class PurchaseSuccessViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    storyRepository: StoryRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<PurchaseSuccessRoute>()

    private val _state = MutableStateFlow(
        PurchaseSuccessUiState(
            storyId = route.storyId,
            progressPercent = route.progressPercent,
            plan = route.plan,
        ),
    )

    val state: StateFlow<PurchaseSuccessUiState> = _state.asStateFlow()

    init {
        storyRepository.story(route.storyId)
            .onEach { onIntent(PurchaseSuccessIntent.StoryChanged(it)) }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: PurchaseSuccessIntent) {
        _state.value = _state.value.reduce(intent)
    }
}
