package se.onemanstudio.playaroundwithai.feature.explore.states

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.google.android.gms.maps.model.LatLng
import kotlinx.collections.immutable.PersistentList
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
data class ExploreUiState(
    val isLoading: Boolean = true,
    val isPathMode: Boolean = false,
    val error: ExploreError? = null,
    val allLocations: PersistentList<ExploreItemUiModel> = persistentListOf(),
    val visibleLocations: PersistentList<ExploreItemUiModel> = persistentListOf(),
    val selectedLocations: PersistentList<ExploreItemUiModel> = persistentListOf(),
    val activeFilter: Set<VehicleType> = persistentSetOf(VehicleType.Scooter, VehicleType.Bicycle),
    val focusedMarker: ExploreItemUiModel? = null,
    val optimalRoute: PersistentList<LatLng> = persistentListOf(),
    val routeDistanceMeters: Int = 0,
    val routeDurationMinutes: Int = 0,
    val suggestedPlaces: PersistentList<SuggestedPlace> = persistentListOf(),
    val focusedSuggestedPlace: SuggestedPlace? = null,
    val suggestedPlacesError: SuggestedPlacesError? = null,
    @StringRes val loadingMessageResId: Int = 0
)
