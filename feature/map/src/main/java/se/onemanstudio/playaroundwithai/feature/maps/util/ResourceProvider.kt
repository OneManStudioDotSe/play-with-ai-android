package se.onemanstudio.playaroundwithai.feature.maps.util

import android.annotation.SuppressLint
import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import se.onemanstudio.playaroundwithai.feature.map.R
import javax.inject.Inject

class ResourceProvider @Inject constructor(private val application: Application) {

    @SuppressLint("MissingPermission")
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = application.getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getLoadingMessages(): List<String> {
        return listOf(
            application.getString(R.string.loading_message_1),
            application.getString(R.string.loading_message_2),
            application.getString(R.string.loading_message_3),
            application.getString(R.string.loading_message_4),
            application.getString(R.string.loading_message_5)
        )
    }
}
