package com.example.nativeminds.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot connectivity check — enough for a sync gate and for deciding whether a missing story
 * body is worth fetching, not a reactive connection state.
 *
 * An interface because connectivity is an *input to a decision* the repository makes, and a
 * decision whose input cannot be set in a test is a decision nobody can prove.
 */
interface NetworkMonitor {
    fun isOnline(): Boolean
}

@Singleton
class AndroidNetworkMonitor @Inject constructor(
    @param:ApplicationContext context: Context,
) : NetworkMonitor {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
