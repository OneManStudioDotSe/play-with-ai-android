package se.onemanstudio.playaroundwithai.feature.maps.state

import com.google.android.gms.maps.model.LatLng
import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.VehicleType
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel

data class MapUiState(
    val isLoading: Boolean = true,
    val isPathMode: Boolean = false,
    val allLocations: List<MapItemUiModel> = emptyList(),
    val visibleLocations: List<MapItemUiModel> = emptyList(),
    val selectedLocations: List<MapItemUiModel> = emptyList(),
    val activeFilter: Set<VehicleType> = setOf(VehicleType.SCOOTER, VehicleType.BICYCLE),
    val focusedMarker: MapItemUiModel? = null,
    val optimalRoute: List<LatLng> = emptyList(),
    val routeDistanceMeters: Int = 0,
    val routeDurationMinutes: Int = 0
)
