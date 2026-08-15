package com.example.nativeminds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nativeminds.domain.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Bridges [ThemeRepository] into the one place that needs it outside a feature module — the
 * `NativeMindsTheme { }` call in [MainActivity], which has no `NavBackStackEntry` of its own.
 */
@HiltViewModel
class AppThemeViewModel @Inject constructor(
    themeRepository: ThemeRepository,
) : ViewModel() {
    val isDarkTheme: StateFlow<Boolean> = themeRepository.isDarkTheme()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
