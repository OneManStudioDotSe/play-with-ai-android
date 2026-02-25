package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val appVersion: String,
    val vehicleCount: Int = DEFAULT_VEHICLE_COUNT,
    val searchRadiusKm: Float = DEFAULT_SEARCH_RADIUS_KM,
) {
    companion object {
        const val MIN_VEHICLE_COUNT = 10
        const val MAX_VEHICLE_COUNT = 100
        const val DEFAULT_VEHICLE_COUNT = 30
        const val MIN_SEARCH_RADIUS_KM = 1.0f
        const val MAX_SEARCH_RADIUS_KM = 10.0f
        const val DEFAULT_SEARCH_RADIUS_KM = 4.0f
    }
}
