package se.onemanstudio.playaroundwithai.data.maps.util

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

class ConnectivityNetworkMonitor @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : NetworkMonitor {

    @SuppressLint("MissingPermission")
    override fun isNetworkAvailable(): Boolean {
        val capabilities = connectivityManager.activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
