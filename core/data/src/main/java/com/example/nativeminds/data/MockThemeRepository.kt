package com.example.nativeminds.data

import com.example.nativeminds.domain.repository.ThemeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Dark-theme preference while there is no local-prefs store.
 *
 * Cut corner: the value lives in memory and resets to light on process death — a
 * DataStore-backed implementation would persist it, but the project has no DataStore dependency
 * yet and this is the only setting that would need one so far. Every read of the current theme
 * already goes through [ThemeRepository], so swapping in a persisted implementation later is a
 * single `@Binds` change.
 */
@Singleton
class MockThemeRepository @Inject constructor() : ThemeRepository {
    private val darkTheme = MutableStateFlow(false)

    override fun isDarkTheme(): Flow<Boolean> = darkTheme.asStateFlow()

    override fun setDarkTheme(value: Boolean) {
        darkTheme.value = value
    }
}
