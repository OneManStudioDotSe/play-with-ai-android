package se.onemanstudio.playaroundwithai.feature.maps.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ConnectivityNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkMonitor {

    @SuppressLint("MissingPermission")
    override fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = connectivityManager.activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
