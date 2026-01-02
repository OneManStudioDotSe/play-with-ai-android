package se.onemanstudio.playaroundwithai.feature.maps.state

import com.google.android.gms.maps.model.LatLng
import se.onemanstudio.playaroundwithai.core.data.feature.map.remote.dto.VehicleType
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItem

data class MapUiState(
    val isLoading: Boolean = true,
    val isPathMode: Boolean = false,
    val allLocations: List<MapItem> = emptyList(),
    val visibleLocations: List<MapItem> = emptyList(),
    val selectedLocations: List<MapItem> = emptyList(),
    val activeFilter: Set<VehicleType> = setOf(VehicleType.SCOOTER, VehicleType.BICYCLE),
    val focusedMarker: MapItem? = null,
    val optimalRoute: List<LatLng> = emptyList(),
    val routeDistanceMeters: Int = 0,
    val routeDurationMinutes: Int = 0
)
