package se.onemanstudio.playaroundwithai.feature.maps.states

import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.LatLng
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel

@Immutable
sealed interface MapError {
    data object NetworkError : MapError
    data class Unknown(val message: String?) : MapError
}

@Immutable
sealed interface SuggestedPlacesError {
    data object LocationUnavailable : SuggestedPlacesError
    data object FetchFailed : SuggestedPlacesError
}

@Immutable
data class MapUiState(
    val isLoading: Boolean = true,
    val isPathMode: Boolean = false,
    val error: MapError? = null,
    val allLocations: PersistentList<MapItemUiModel> = persistentListOf(),
    val visibleLocations: PersistentList<MapItemUiModel> = persistentListOf(),
    val selectedLocations: PersistentList<MapItemUiModel> = persistentListOf(),
    val activeFilter: Set<VehicleType> = persistentSetOf(VehicleType.SCOOTER, VehicleType.BICYCLE),
    val focusedMarker: MapItemUiModel? = null,
    val optimalRoute: PersistentList<LatLng> = persistentListOf(),
    val routeDistanceMeters: Int = 0,
    val routeDurationMinutes: Int = 0,
    val suggestedPlaces: PersistentList<SuggestedPlace> = persistentListOf(),
    val focusedSuggestedPlace: SuggestedPlace? = null,
    val suggestedPlacesError: SuggestedPlacesError? = null
)
