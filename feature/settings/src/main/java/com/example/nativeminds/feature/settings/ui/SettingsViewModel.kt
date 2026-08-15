package com.example.nativeminds.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nativeminds.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())

    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val effectChannel = Channel<SettingsEffect>(Channel.BUFFERED)

    val effects: Flow<SettingsEffect> = effectChannel.receiveAsFlow()

    init {
        themeRepository.isDarkTheme()
            .onEach { onIntent(SettingsIntent.ThemeChanged(it)) }
            .launchIn(viewModelScope)
    }

    /**
     * The toggle's write to [ThemeRepository] happens here, keyed to the one intent that means
     * "flip the theme" — the reducer already applied the new value to state, this only persists it.
     */
    fun onIntent(intent: SettingsIntent) {
        val reduction = _state.value.reduce(intent)
        _state.value = reduction.state
        if (intent is SettingsIntent.ThemeToggleClicked) {
            themeRepository.setDarkTheme(reduction.state.isDarkTheme)
        }
        reduction.effects.forEach(effectChannel::trySend)
    }
}
