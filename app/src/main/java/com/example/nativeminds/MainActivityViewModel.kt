package com.example.nativeminds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nativeminds.domain.usecase.EnsureContentLanguageUseCase
import com.example.nativeminds.domain.usecase.SyncLessonsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/** Generous enough for a ~40-row catalog fetch and every lesson's text (~30KB total) on a normal
 * connection, short enough that a stalled or absent network can never make the splash look frozen
 * — the sync that times out here just continues in the background via
 * [com.example.nativeminds.feature.home.ui.HomeViewModel]'s own. */
private const val INITIAL_SYNC_TIMEOUT_MS = 4_000L

/**
 * Holds the splash screen up in [MainActivity] until the local lesson cache is both in the right
 * language and actually populated — lessons and every lesson's text — so Home's first frame has
 * lessons to show instead of the "never synced" empty state, and every lesson is readable offline
 * from the start, on the common case of a normal-speed connection.
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val ensureContentLanguage: EnsureContentLanguageUseCase,
    private val syncLessons: SyncLessonsUseCase,
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            ensureContentLanguage()
            withTimeoutOrNull(INITIAL_SYNC_TIMEOUT_MS.milliseconds) { syncLessons() }
            _isReady.value = true
        }
    }
}
