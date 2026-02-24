package se.onemanstudio.playaroundwithai.feature.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase.GetMapItemsUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase.GetSuggestedPlacesUseCase
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel
import se.onemanstudio.playaroundwithai.feature.maps.models.toUiModel
import se.onemanstudio.playaroundwithai.feature.maps.states.MapError
import se.onemanstudio.playaroundwithai.feature.maps.states.MapUiState
import se.onemanstudio.playaroundwithai.feature.maps.states.SuggestedPlacesError
import se.onemanstudio.playaroundwithai.feature.maps.util.ResourceProvider
import se.onemanstudio.playaroundwithai.feature.maps.utils.calculatePathDistance
import se.onemanstudio.playaroundwithai.feature.maps.utils.permutations
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt

private const val AMOUNT_OF_POINTS_TO_GENERATE = 30
private const val WALKING_SPEED_METERS_PER_MIN = 83.0 // approx 5km/h
private const val LOADING_MESSAGE_DURATION = 3000L

@Suppress("TooManyFunctions")
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMapItemsUseCase: GetMapItemsUseCase,
    private val getSuggestedPlacesUseCase: GetSuggestedPlacesUseCase,
    private val apiKeyAvailability: ApiKeyAvailability,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        startLoadingMessageCycle()
    }

    @Suppress("TooGenericExceptionCaught")
    fun loadMapData(centerLat: Double, centerLng: Double) {
        if (!apiKeyAvailability.isMapsKeyAvailable) {
            _uiState.update { it.copy(isLoading = false, error = MapError.ApiKeyMissing) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        if (!resourceProvider.isNetworkAvailable()) {
            Timber.w("MapViewModel - No network available, cannot load map data")
            _uiState.update { it.copy(isLoading = false, error = MapError.NetworkError) }
            return
        }

        viewModelScope.launch {
            try {
                val data = getMapItemsUseCase(AMOUNT_OF_POINTS_TO_GENERATE, centerLat, centerLng)
                    .map { it.toUiModel() }.toPersistentList()
                _uiState.update {
                    it.copy(isLoading = false, allLocations = data, visibleLocations = data)
                }
            } catch (e: IOException) {
                Timber.e(e, "MapViewModel - Failed to load map data (network)")
                _uiState.update { it.copy(isLoading = false, error = MapError.NetworkError) }
            } catch (e: Exception) {
                Timber.e(e, "MapViewModel - Failed to load map data")
                _uiState.update { it.copy(isLoading = false, error = MapError.Unknown(e.localizedMessage)) }
            }
        }
    }

    fun setPathMode(active: Boolean) {
        _uiState.update {
            it.copy(
                isPathMode = active,
                focusedMarker = null,
                focusedSuggestedPlace = null,
                selectedLocations = persistentListOf(),
                optimalRoute = persistentListOf(),
                routeDistanceMeters = 0,
                routeDurationMinutes = 0,
            )
        }
    }

    fun selectMarker(marker: MapItemUiModel?) {
        _uiState.update { it.copy(focusedMarker = marker, focusedSuggestedPlace = null) }
    }

    fun toggleFilter(type: VehicleType) {
        _uiState.update { currentState ->
            val newFilters = if (currentState.activeFilter.contains(type)) {
                currentState.activeFilter - type
            } else {
                currentState.activeFilter + type
            }

            val filtered = currentState.allLocations.filter { newFilters.contains(it.type) }.toPersistentList()

            currentState.copy(
                activeFilter = newFilters,
                visibleLocations = filtered,
                selectedLocations = persistentListOf(),
                optimalRoute = persistentListOf(),
                focusedMarker = null
            )
        }
    }

    fun toggleSelection(location: MapItemUiModel) {
        if (!_uiState.value.isPathMode) return

        _uiState.update { state ->
            val currentSelected = state.selectedLocations
            val isAlreadySelected = currentSelected.any { it.id == location.id }

            val newSelected = if (isAlreadySelected) {
                currentSelected.filter { it.id != location.id }.toPersistentList()
            } else {
                if (currentSelected.size < MapConstants.MAX_SELECTABLE_POINTS) {
                    (currentSelected + location.copy(isSelected = true)).toPersistentList()
                } else {
                    currentSelected // Limit reached, do not add
                }
            }
            state.copy(
                selectedLocations = newSelected,
                optimalRoute = persistentListOf(),
                routeDistanceMeters = 0
            )
        }
    }

    fun toggleSuggestedPlaceSelection(place: SuggestedPlace) {
        if (!_uiState.value.isPathMode) return

        val syntheticId = "suggested_${place.name}_${place.lat}_${place.lng}"
        val syntheticUiModel = MapItemUiModel(
            mapItem = MapItem(
                id = syntheticId,
                lat = place.lat,
                lng = place.lng,
                name = place.name,
                type = VehicleType.SCOOTER,
                batteryLevel = 0,
                vehicleCode = "",
                nickname = place.name
            ),
            isSelected = true
        )

        _uiState.update { state ->
            val currentSelected = state.selectedLocations
            val isAlreadySelected = currentSelected.any { it.id == syntheticId }

            val newSelected = if (isAlreadySelected) {
                currentSelected.filter { it.id != syntheticId }.toPersistentList()
            } else {
                if (currentSelected.size < MapConstants.MAX_SELECTABLE_POINTS) {
                    (currentSelected + syntheticUiModel).toPersistentList()
                } else {
                    currentSelected
                }
            }
            state.copy(
                selectedLocations = newSelected,
                optimalRoute = persistentListOf(),
                routeDistanceMeters = 0
            )
        }
    }

    fun calculateOptimalRoute(userLocation: LatLng?) {
        val points = _uiState.value.selectedLocations.map { it.position }
        if (points.isEmpty()) return

        val startPoint = userLocation ?: points.first()
        // Ensure we visit all selected points, starting from user location
        val pointsToVisit = points

        val bestPermutation = permutations(pointsToVisit)
            .minByOrNull { path -> calculatePathDistance(startPoint, path) }
            ?: pointsToVisit

        val fullPath = (listOf(startPoint) + bestPermutation).toPersistentList()
        val totalDistance = calculatePathDistance(startPoint, bestPermutation)

        _uiState.update {
            it.copy(
                optimalRoute = fullPath,
                routeDistanceMeters = (totalDistance * 1000).roundToInt(),
                routeDurationMinutes = ((totalDistance * 1000) / WALKING_SPEED_METERS_PER_MIN).roundToInt()
            )
        }
    }

    fun getAiSuggestedPlaces(userLocation: LatLng?) {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _uiState.update { it.copy(suggestedPlacesError = SuggestedPlacesError.FetchFailed) }
            return
        }

        if (userLocation == null) {
            _uiState.update {
                it.copy(focusedSuggestedPlace = null, suggestedPlacesError = SuggestedPlacesError.LocationUnavailable)
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                focusedMarker = null,
                suggestedPlaces = persistentListOf(),
                focusedSuggestedPlace = null,
                suggestedPlacesError = null
            )
        }

        viewModelScope.launch {
            getSuggestedPlacesUseCase(userLocation.latitude, userLocation.longitude)
                .onSuccess { places ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            suggestedPlaces = places.toPersistentList(),
                            suggestedPlacesError = null
                        )
                    }
                }
                .onFailure { exception ->
                    Timber.e(exception, "Failed to get AI suggested places")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            suggestedPlacesError = SuggestedPlacesError.FetchFailed
                        )
                    }
                }
        }
    }

    fun dismissSuggestedPlacesError() {
        _uiState.update { it.copy(suggestedPlacesError = null) }
    }

    fun selectSuggestedPlace(place: SuggestedPlace?) {
        _uiState.update { it.copy(focusedMarker = null, focusedSuggestedPlace = place) }
    }

    private fun startLoadingMessageCycle() {
        viewModelScope.launch {
            val messages = resourceProvider.getLoadingMessages()
            var index = 0
            while (true) {
                _uiState.update { it.copy(loadingMessage = messages[index]) }
                delay(LOADING_MESSAGE_DURATION)
                index = (index + 1) % messages.size
            }
        }
    }
}
