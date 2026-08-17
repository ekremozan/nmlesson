package com.example.nativeminds.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "lesson_sync_prefs"
private const val KEY_LAST_SYNCED_LANGUAGE = "last_synced_language"

/**
 * The language the local lesson cache was last synced in, so a repository can tell a genuine
 * language switch apart from an ordinary refresh — the two need different cache-clearing behavior.
 *
 * An interface for the same reason as [NetworkMonitor]: a test needs to set this without touching
 * real `SharedPreferences`.
 */
interface LastSyncedLanguageStore {
    fun get(): String?

    fun set(language: String)
}

@Singleton
class SharedPreferencesLastSyncedLanguageStore @Inject constructor(
    @ApplicationContext context: Context,
) : LastSyncedLanguageStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun get(): String? = prefs.getString(KEY_LAST_SYNCED_LANGUAGE, null)

    override fun set(language: String) {
        prefs.edit().putString(KEY_LAST_SYNCED_LANGUAGE, language).apply()
    }
}
