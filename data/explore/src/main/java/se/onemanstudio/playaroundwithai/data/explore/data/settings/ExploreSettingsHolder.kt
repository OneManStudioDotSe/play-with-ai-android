package se.onemanstudio.playaroundwithai.data.explore.data.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_VEHICLE_COUNT = 30
private const val DEFAULT_SEARCH_RADIUS_KM = 4.0f

@Singleton
class ExploreSettingsHolder @Inject constructor() {

    private val _vehicleCount = MutableStateFlow(DEFAULT_VEHICLE_COUNT)
    val vehicleCount: StateFlow<Int> = _vehicleCount.asStateFlow()

    private val _searchRadiusKm = MutableStateFlow(DEFAULT_SEARCH_RADIUS_KM)
    val searchRadiusKm: StateFlow<Float> = _searchRadiusKm.asStateFlow()

    fun updateVehicleCount(count: Int) {
        _vehicleCount.value = count
    }

    fun updateSearchRadiusKm(radius: Float) {
        _searchRadiusKm.value = radius
    }
}
