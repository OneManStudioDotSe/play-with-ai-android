package se.onemanstudio.playaroundwithai.feature.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val appVersion: String,
    val showTokenUsage: Boolean = false,
    val vehicleCount: Int = DEFAULT_VEHICLE_COUNT,
    val searchRadiusKm: Float = DEFAULT_SEARCH_RADIUS_KM,
    val walkingSpeedKmh: Float = WALKING_SPEED_NORMAL,
    val typewriterDelayMs: Long = TYPEWRITER_DELAY_NORMAL,
    val hapticFeedbackEnabled: Boolean = true,
    val networkTimeoutSeconds: Int = DEFAULT_NETWORK_TIMEOUT_SECONDS,
    val tokenTrackingEnabled: Boolean = true,
    val tripLengthMinStops: Int = TRIP_LENGTH_STANDARD_MIN,
) {
    companion object {
        const val MIN_VEHICLE_COUNT = 10
        const val MAX_VEHICLE_COUNT = 100
        const val DEFAULT_VEHICLE_COUNT = 30
        const val MIN_SEARCH_RADIUS_KM = 1.0f
        const val MAX_SEARCH_RADIUS_KM = 10.0f
        const val DEFAULT_SEARCH_RADIUS_KM = 4.0f
        const val WALKING_SPEED_SLOW = 3.0f
        const val WALKING_SPEED_NORMAL = 5.0f
        const val WALKING_SPEED_FAST = 7.0f
        const val TYPEWRITER_DELAY_INSTANT = 0L
        const val TYPEWRITER_DELAY_FAST = 5L
        const val TYPEWRITER_DELAY_NORMAL = 10L
        const val TYPEWRITER_DELAY_SLOW = 30L
        const val MIN_NETWORK_TIMEOUT_SECONDS = 15
        const val MAX_NETWORK_TIMEOUT_SECONDS = 120
        const val DEFAULT_NETWORK_TIMEOUT_SECONDS = 30
        const val NETWORK_TIMEOUT_STEP_SECONDS = 15
        const val TRIP_LENGTH_QUICK_MIN = 2
        const val TRIP_LENGTH_STANDARD_MIN = 4
        const val TRIP_LENGTH_EXTENDED_MIN = 7
    }
}
