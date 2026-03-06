package se.onemanstudio.playaroundwithai.feature.explore.states

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.LatLng
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import se.onemanstudio.playaroundwithai.data.explore.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.feature.explore.models.ExploreItemUiModel

@Immutable
sealed interface ExploreError {
    data object ApiKeyMissing : ExploreError
    data object NetworkError : ExploreError
    data class Unknown(val message: String?) : ExploreError
}

@Immutable
sealed interface SuggestedPlacesError {
    data object LocationUnavailable : SuggestedPlacesError
    data object FetchFailed : SuggestedPlacesError
}

@Immutable
data class MarkersState(
    val isLoading: Boolean = true,
    val allLocations: PersistentList<ExploreItemUiModel> = persistentListOf(),
    val visibleLocations: PersistentList<ExploreItemUiModel> = persistentListOf(),
    val activeFilter: PersistentSet<VehicleType> = persistentSetOf(VehicleType.Scooter, VehicleType.Bicycle),
    val focusedMarker: ExploreItemUiModel? = null,
)

@Immutable
data class PathModeState(
    val isActive: Boolean = false,
    val selectedLocations: PersistentList<ExploreItemUiModel> = persistentListOf(),
    val optimalRoute: PersistentList<LatLng> = persistentListOf(),
    val routeDistanceMeters: Int = 0,
    val routeDurationMinutes: Int = 0,
)

@Immutable
data class SuggestionsState(
    val isLoading: Boolean = false,
    @get:StringRes val loadingMessageResId: Int? = null,
    val places: PersistentList<SuggestedPlace> = persistentListOf(),
    val focusedPlace: SuggestedPlace? = null,
    val error: SuggestedPlacesError? = null,
)

@Immutable
data class ExploreUiState(
    val markers: MarkersState = MarkersState(),
    val pathMode: PathModeState = PathModeState(),
    val suggestions: SuggestionsState = SuggestionsState(),
    val error: ExploreError? = null,
)
