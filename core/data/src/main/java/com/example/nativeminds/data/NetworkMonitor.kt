package com.example.nativeminds.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** One-shot connectivity check — enough for a startup sync gate, not a reactive connection state. */
class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
