package se.onemanstudio.playaroundwithai.core.config.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

const val WALKING_SPEED_KMH_DEFAULT = 5.0f
const val TYPEWRITER_DELAY_MS_DEFAULT = 10L
const val HAPTIC_FEEDBACK_ENABLED_DEFAULT = true
const val NETWORK_TIMEOUT_SECONDS_DEFAULT = 30
const val TOKEN_TRACKING_ENABLED_DEFAULT = true

@Singleton
class AppSettingsHolder @Inject constructor() {

    private val _showTokenUsage = MutableStateFlow(false)
    val showTokenUsage: StateFlow<Boolean> = _showTokenUsage.asStateFlow()

    private val _walkingSpeedKmh = MutableStateFlow(WALKING_SPEED_KMH_DEFAULT)
    val walkingSpeedKmh: StateFlow<Float> = _walkingSpeedKmh.asStateFlow()

    private val _typewriterDelayMs = MutableStateFlow(TYPEWRITER_DELAY_MS_DEFAULT)
    val typewriterDelayMs: StateFlow<Long> = _typewriterDelayMs.asStateFlow()

    private val _hapticFeedbackEnabled = MutableStateFlow(HAPTIC_FEEDBACK_ENABLED_DEFAULT)
    val hapticFeedbackEnabled: StateFlow<Boolean> = _hapticFeedbackEnabled.asStateFlow()

    private val _networkTimeoutSeconds = MutableStateFlow(NETWORK_TIMEOUT_SECONDS_DEFAULT)
    val networkTimeoutSeconds: StateFlow<Int> = _networkTimeoutSeconds.asStateFlow()

    private val _tokenTrackingEnabled = MutableStateFlow(TOKEN_TRACKING_ENABLED_DEFAULT)
    val tokenTrackingEnabled: StateFlow<Boolean> = _tokenTrackingEnabled.asStateFlow()

    fun updateShowTokenUsage(enabled: Boolean) {
        _showTokenUsage.value = enabled
    }

    fun updateWalkingSpeedKmh(speedKmh: Float) {
        _walkingSpeedKmh.value = speedKmh
    }

    fun updateTypewriterDelayMs(delayMs: Long) {
        _typewriterDelayMs.value = delayMs
    }

    fun updateHapticFeedbackEnabled(enabled: Boolean) {
        _hapticFeedbackEnabled.value = enabled
    }

    fun updateNetworkTimeoutSeconds(seconds: Int) {
        _networkTimeoutSeconds.value = seconds
    }

    fun updateTokenTrackingEnabled(enabled: Boolean) {
        _tokenTrackingEnabled.value = enabled
    }
}
