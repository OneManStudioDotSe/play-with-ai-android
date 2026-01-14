package se.onemanstudio.playaroundwithai.feature.maps.state

import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.LatLng
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import se.onemanstudio.playaroundwithai.core.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel

@Immutable
data class MapUiState(
    val isLoading: Boolean = true,
    val isPathMode: Boolean = false,
    val allLocations: PersistentList<MapItemUiModel> = persistentListOf(),
    val visibleLocations: PersistentList<MapItemUiModel> = persistentListOf(),
    val selectedLocations: PersistentList<MapItemUiModel> = persistentListOf(),
    val activeFilter: PersistentSet<VehicleType> = persistentSetOf(VehicleType.SCOOTER, VehicleType.BICYCLE),
    val focusedMarker: MapItemUiModel? = null,
    val optimalRoute: PersistentList<LatLng> = persistentListOf(),
    val routeDistanceMeters: Int = 0,
    val routeDurationMinutes: Int = 0
)
