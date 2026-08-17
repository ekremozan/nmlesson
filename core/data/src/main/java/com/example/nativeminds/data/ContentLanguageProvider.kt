package com.example.nativeminds.data

import java.util.Locale
import javax.inject.Inject

/**
 * Which language the remote lesson catalog should be fetched in.
 *
 * An interface for the same reason as [NetworkMonitor]: the repository's fetch decision needs an
 * input that a test can set, rather than a static [Locale] read buried inside the query itself.
 */
interface ContentLanguageProvider {
    /** A Supabase `language` column value — currently either `"tr"` or `"en"`. */
    fun current(): String
}

class DeviceContentLanguageProvider @Inject constructor() : ContentLanguageProvider {
    override fun current(): String =
        if (Locale.getDefault().language == "en") "en" else "tr"
}
