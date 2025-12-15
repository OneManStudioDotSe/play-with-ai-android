package se.onemanstudio.playaroundwithai.feature.maps.state

import com.google.android.gms.maps.model.LatLng
import se.onemanstudio.playaroundwithai.feature.maps.models.ItemOnMap
import se.onemanstudio.playaroundwithai.feature.maps.models.VehicleType

data class MapUiState(
    val isLoading: Boolean = true,
    val isPathMode: Boolean = false,
    val allLocations: List<ItemOnMap> = emptyList(),
    val visibleLocations: List<ItemOnMap> = emptyList(),
    val selectedLocations: List<ItemOnMap> = emptyList(),
    val activeFilter: Set<VehicleType> = setOf(VehicleType.SCOOTER, VehicleType.BICYCLE),
    val focusedMarker: ItemOnMap? = null,
    val optimalRoute: List<LatLng> = emptyList(),
    val routeDistanceMeters: Int = 0,
    val routeDurationMinutes: Int = 0
)
