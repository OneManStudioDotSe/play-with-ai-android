package se.onemanstudio.playaroundwithai.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import se.onemanstudio.playaroundwithai.core.config.di.AppVersion
import se.onemanstudio.playaroundwithai.core.config.settings.AppSettingsHolder
import se.onemanstudio.playaroundwithai.core.tracking.model.DailyTokenUsage
import se.onemanstudio.playaroundwithai.core.tracking.usecase.GetWeeklyTokenUsageUseCase
import se.onemanstudio.playaroundwithai.core.config.settings.ExploreSettingsHolder
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
class SettingsViewModel @Inject constructor(
    getWeeklyTokenUsageUseCase: GetWeeklyTokenUsageUseCase,
    private val exploreSettingsHolder: ExploreSettingsHolder,
    private val appSettingsHolder: AppSettingsHolder,
    @param: AppVersion val appVersion: String,
) : ViewModel() {

    val weeklyUsage: StateFlow<List<DailyTokenUsage>> = getWeeklyTokenUsageUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val showTokenUsage: StateFlow<Boolean> = appSettingsHolder.showTokenUsage
    val vehicleCount: StateFlow<Int> = exploreSettingsHolder.vehicleCount
    val searchRadiusKm: StateFlow<Float> = exploreSettingsHolder.searchRadiusKm
    val walkingSpeedKmh: StateFlow<Float> = appSettingsHolder.walkingSpeedKmh
    val typewriterDelayMs: StateFlow<Long> = appSettingsHolder.typewriterDelayMs
    val hapticFeedbackEnabled: StateFlow<Boolean> = appSettingsHolder.hapticFeedbackEnabled
    val networkTimeoutSeconds: StateFlow<Int> = appSettingsHolder.networkTimeoutSeconds
    val tokenTrackingEnabled: StateFlow<Boolean> = appSettingsHolder.tokenTrackingEnabled
    val tripLengthMinStops: StateFlow<Int> = appSettingsHolder.tripLengthMinStops
    val firebaseSyncEnabled: StateFlow<Boolean> = appSettingsHolder.firebaseSyncEnabled
    val imageQualityJpeg: StateFlow<Int> = appSettingsHolder.imageQualityJpeg
    val agentMaxIterations: StateFlow<Int> = appSettingsHolder.agentMaxIterations
    val suggestedPlacesCount: StateFlow<Int> = appSettingsHolder.suggestedPlacesCount
    val maxSelectablePoints: StateFlow<Int> = exploreSettingsHolder.maxSelectablePoints

    private val _selectedDayIndex = MutableStateFlow<Int?>(null)
    val selectedDayIndex: StateFlow<Int?> = _selectedDayIndex

    fun onBarTapped(index: Int) {
        _selectedDayIndex.value = if (_selectedDayIndex.value == index) null else index
    }

    fun onVehicleCountChange(count: Int) {
        exploreSettingsHolder.updateVehicleCount(count)
    }

    fun onSearchRadiusChange(radius: Float) {
        exploreSettingsHolder.updateSearchRadiusKm(radius)
    }

    fun onShowTokenUsageChange(enabled: Boolean) {
        appSettingsHolder.updateShowTokenUsage(enabled)
    }

    fun onWalkingSpeedChange(speedKmh: Float) {
        appSettingsHolder.updateWalkingSpeedKmh(speedKmh)
    }

    fun onTypewriterDelayChange(delayMs: Long) {
        appSettingsHolder.updateTypewriterDelayMs(delayMs)
    }

    fun onHapticFeedbackChange(enabled: Boolean) {
        appSettingsHolder.updateHapticFeedbackEnabled(enabled)
    }

    fun onNetworkTimeoutChange(seconds: Int) {
        appSettingsHolder.updateNetworkTimeoutSeconds(seconds)
    }

    fun onTokenTrackingChange(enabled: Boolean) {
        appSettingsHolder.updateTokenTrackingEnabled(enabled)
    }

    fun onTripLengthChange(minStops: Int) {
        appSettingsHolder.updateTripLengthMinStops(minStops)
    }

    fun onFirebaseSyncChange(enabled: Boolean) {
        appSettingsHolder.updateFirebaseSyncEnabled(enabled)
    }

    fun onImageQualityChange(quality: Int) {
        appSettingsHolder.updateImageQualityJpeg(quality)
    }

    fun onAgentMaxIterationsChange(maxIterations: Int) {
        appSettingsHolder.updateAgentMaxIterations(maxIterations)
    }

    fun onSuggestedPlacesCountChange(count: Int) {
        appSettingsHolder.updateSuggestedPlacesCount(count)
    }

    fun onMaxSelectablePointsChange(max: Int) {
        exploreSettingsHolder.updateMaxSelectablePoints(max)
    }

    fun onResetToDefaults() {
        appSettingsHolder.updateShowTokenUsage(false)
        appSettingsHolder.updateWalkingSpeedKmh(SettingsState.WALKING_SPEED_NORMAL)
        appSettingsHolder.updateTypewriterDelayMs(SettingsState.TYPEWRITER_DELAY_NORMAL)
        appSettingsHolder.updateHapticFeedbackEnabled(true)
        appSettingsHolder.updateNetworkTimeoutSeconds(SettingsState.DEFAULT_NETWORK_TIMEOUT_SECONDS)
        appSettingsHolder.updateTokenTrackingEnabled(true)
        appSettingsHolder.updateTripLengthMinStops(SettingsState.TRIP_LENGTH_STANDARD_MIN)
        appSettingsHolder.updateFirebaseSyncEnabled(true)
        appSettingsHolder.updateImageQualityJpeg(SettingsState.IMAGE_QUALITY_MEDIUM)
        appSettingsHolder.updateAgentMaxIterations(SettingsState.AGENT_ITERATIONS_STANDARD)
        appSettingsHolder.updateSuggestedPlacesCount(SettingsState.DEFAULT_SUGGESTED_PLACES_COUNT)
        exploreSettingsHolder.updateVehicleCount(SettingsState.DEFAULT_VEHICLE_COUNT)
        exploreSettingsHolder.updateSearchRadiusKm(SettingsState.DEFAULT_SEARCH_RADIUS_KM)
        exploreSettingsHolder.updateMaxSelectablePoints(SettingsState.DEFAULT_MAX_SELECTABLE_POINTS)
    }
}
