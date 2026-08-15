package com.example.nativeminds.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for whether the app is currently in dark theme.
 *
 * `:app` reads this to pick [com.example.nativeminds.designsystem.theme.NativeMindsTheme]'s
 * `darkTheme` argument, and the settings screen's toggle is the only writer — no screen keeps its
 * own copy of the current theme.
 */
interface ThemeRepository {
    fun isDarkTheme(): Flow<Boolean>

    fun setDarkTheme(value: Boolean)
}
