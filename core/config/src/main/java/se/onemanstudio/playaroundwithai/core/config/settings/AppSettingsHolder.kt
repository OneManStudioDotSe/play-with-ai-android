package se.onemanstudio.playaroundwithai.core.config.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

const val WALKING_SPEED_KMH_DEFAULT = 5.0f

@Singleton
class AppSettingsHolder @Inject constructor() {

    private val _showTokenUsage = MutableStateFlow(false)
    val showTokenUsage: StateFlow<Boolean> = _showTokenUsage.asStateFlow()

    private val _walkingSpeedKmh = MutableStateFlow(WALKING_SPEED_KMH_DEFAULT)
    val walkingSpeedKmh: StateFlow<Float> = _walkingSpeedKmh.asStateFlow()

    fun updateShowTokenUsage(enabled: Boolean) {
        _showTokenUsage.value = enabled
    }

    fun updateWalkingSpeedKmh(speedKmh: Float) {
        _walkingSpeedKmh.value = speedKmh
    }
}
