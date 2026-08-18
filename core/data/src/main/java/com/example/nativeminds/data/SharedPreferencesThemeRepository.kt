package com.example.nativeminds.data

import android.content.Context
import androidx.core.content.edit
import com.example.nativeminds.domain.repository.ThemeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "theme_prefs"
private const val KEY_IS_DARK_THEME = "is_dark_theme"

/**
 * Dark-theme preference, persisted via [android.content.SharedPreferences].
 *
 * Every read of the current theme already goes through [ThemeRepository], so this mirrors
 * [SharedPreferencesEntitlementRepository]'s shape rather than introducing a DataStore dependency
 * for a single boolean.
 */
@Singleton
class SharedPreferencesThemeRepository @Inject constructor(
    @ApplicationContext context: Context,
) : ThemeRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val darkTheme = MutableStateFlow(prefs.getBoolean(KEY_IS_DARK_THEME, false))

    override fun isDarkTheme(): Flow<Boolean> = darkTheme.asStateFlow()

    override fun setDarkTheme(value: Boolean) {
        prefs.edit { putBoolean(KEY_IS_DARK_THEME, value) }
        darkTheme.value = value
    }
}
