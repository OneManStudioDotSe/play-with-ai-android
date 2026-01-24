package se.onemanstudio.playaroundwithai.feature.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase.GetMapItemsUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase.GetSuggestedPlacesUseCase
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel
import se.onemanstudio.playaroundwithai.feature.maps.models.toUiModel
import se.onemanstudio.playaroundwithai.feature.maps.states.MapUiState
import se.onemanstudio.playaroundwithai.feature.maps.utils.calculatePathDistance
import se.onemanstudio.playaroundwithai.feature.maps.utils.permutations
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

private const val AMOUNT_OF_POINTS_TO_GENERATE = 30
private const val WALKING_SPEED_METERS_PER_MIN = 83.0 // approx 5km/h

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMapItemsUseCase: GetMapItemsUseCase,
    private val getSuggestedPlacesUseCase: GetSuggestedPlacesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMapData()
    }

    fun loadMapData() {
        _uiState.update {
            it.copy(
                isLoading = true,
                isPathMode = it.isPathMode,
                allLocations = it.allLocations,
                visibleLocations = it.visibleLocations,
                selectedLocations = it.selectedLocations,
                activeFilter = it.activeFilter,
                focusedMarker = null,
                optimalRoute = it.optimalRoute,
                routeDistanceMeters = it.routeDistanceMeters,
                routeDurationMinutes = it.routeDurationMinutes,
                suggestedPlaces = it.suggestedPlaces,
                focusedSuggestedPlace = null, 
                showLocationError = false
            )
        }
        viewModelScope.launch {
            val data = getMapItemsUseCase(AMOUNT_OF_POINTS_TO_GENERATE).map { it.toUiModel() }.toPersistentList()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPathMode = it.isPathMode,
                    allLocations = data,
                    visibleLocations = data,
                    selectedLocations = it.selectedLocations,
                    activeFilter = it.activeFilter,
                    focusedMarker = it.focusedMarker,
                    optimalRoute = it.optimalRoute,
                    routeDistanceMeters = it.routeDistanceMeters,
                    routeDurationMinutes = it.routeDurationMinutes,
                    suggestedPlaces = it.suggestedPlaces,
                    focusedSuggestedPlace = it.focusedSuggestedPlace,
                    showLocationError = it.showLocationError
                )
            }
        }
    }

    fun setPathMode(active: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = it.isLoading,
                isPathMode = active,
                allLocations = it.allLocations,
                visibleLocations = it.visibleLocations,
                selectedLocations = persistentListOf(),
                activeFilter = it.activeFilter,
                focusedMarker = null,
                optimalRoute = persistentListOf(),
                routeDistanceMeters = it.routeDistanceMeters,
                routeDurationMinutes = it.routeDurationMinutes,
                suggestedPlaces = persistentListOf(),
                focusedSuggestedPlace = null,
                showLocationError = false
            )
        }
    }

    fun selectMarker(marker: MapItemUiModel?) {
        _uiState.update {
            it.copy(
                isLoading = it.isLoading,
                isPathMode = it.isPathMode,
                allLocations = it.allLocations,
                visibleLocations = it.visibleLocations,
                selectedLocations = it.selectedLocations,
                activeFilter = it.activeFilter,
                focusedMarker = marker,
                optimalRoute = it.optimalRoute,
                routeDistanceMeters = it.routeDistanceMeters,
                routeDurationMinutes = it.routeDurationMinutes,
                suggestedPlaces = it.suggestedPlaces,
                focusedSuggestedPlace = null, // Clear focused AI place
                showLocationError = it.showLocationError
            )
        }
    }

    fun selectSuggestedPlace(place: SuggestedPlace?) {
        _uiState.update {
            it.copy(
                isLoading = it.isLoading,
                isPathMode = it.isPathMode,
                allLocations = it.allLocations,
                visibleLocations = it.visibleLocations,
                selectedLocations = it.selectedLocations,
                activeFilter = it.activeFilter,
                focusedMarker = null, // Clear focused regular marker
                optimalRoute = it.optimalRoute,
                routeDistanceMeters = it.routeDistanceMeters,
                routeDurationMinutes = it.routeDurationMinutes,
                suggestedPlaces = it.suggestedPlaces,
                focusedSuggestedPlace = place,
                showLocationError = it.showLocationError
            )
        }
    }

    fun toggleFilter(type: VehicleType) {
        _uiState.update { currentState ->
            val newFilters = if (currentState.activeFilter.contains(type)) {
                (currentState.activeFilter - type).toPersistentSet()
            } else {
                (currentState.activeFilter + type).toPersistentSet()
            }

            val filtered = currentState.allLocations.filter { newFilters.contains(it.type) }.toPersistentList()

            currentState.copy(
                isLoading = currentState.isLoading,
                isPathMode = currentState.isPathMode,
                allLocations = currentState.allLocations,
                visibleLocations = filtered,
                selectedLocations = persistentListOf(),
                activeFilter = newFilters,
                focusedMarker = null,
                optimalRoute = persistentListOf(),
                routeDistanceMeters = currentState.routeDistanceMeters,
                routeDurationMinutes = currentState.routeDurationMinutes,
                suggestedPlaces = persistentListOf(),
                focusedSuggestedPlace = null, // Clear focused AI place
                showLocationError = false
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
                    currentSelected
                }
            }
            state.copy(
                isLoading = state.isLoading,
                isPathMode = state.isPathMode,
                allLocations = state.allLocations,
                visibleLocations = state.visibleLocations,
                selectedLocations = newSelected,
                activeFilter = state.activeFilter,
                focusedMarker = state.focusedMarker,
                optimalRoute = persistentListOf(),
                routeDistanceMeters = 0,
                routeDurationMinutes = state.routeDurationMinutes,
                suggestedPlaces = persistentListOf(),
                focusedSuggestedPlace = null, // Clear focused AI place
                showLocationError = false
            )
        }
    }

    fun calculateOptimalRoute(userLocation: LatLng?) {
        val points = _uiState.value.selectedLocations.map { it.position }
        if (points.isEmpty()) return

        _uiState.update {
            it.copy(
                isLoading = true,
                isPathMode = it.isPathMode,
                allLocations = it.allLocations,
                visibleLocations = it.visibleLocations,
                selectedLocations = it.selectedLocations,
                activeFilter = it.activeFilter,
                focusedMarker = it.focusedMarker,
                optimalRoute = it.optimalRoute,
                routeDistanceMeters = it.routeDistanceMeters,
                routeDurationMinutes = it.routeDurationMinutes,
                suggestedPlaces = it.suggestedPlaces,
                focusedSuggestedPlace = it.focusedSuggestedPlace, // Keep existing
                showLocationError = it.showLocationError
            )
        }

        viewModelScope.launch {
            val startPoint = userLocation ?: points.first()
            val pointsToVisit = points

            val bestPermutation = permutations(pointsToVisit)
                .minByOrNull { path -> calculatePathDistance(startPoint, path) }
                ?: pointsToVisit

            val fullPath = (listOf(startPoint) + bestPermutation).toPersistentList()
            val totalDistance = calculatePathDistance(startPoint, bestPermutation)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isPathMode = it.isPathMode,
                    allLocations = it.allLocations,
                    visibleLocations = it.visibleLocations,
                    selectedLocations = it.selectedLocations,
                    activeFilter = it.activeFilter,
                    focusedMarker = it.focusedMarker,
                    optimalRoute = fullPath,
                    routeDistanceMeters = (totalDistance * 1000).roundToInt(),
                    routeDurationMinutes = ((totalDistance * 1000) / WALKING_SPEED_METERS_PER_MIN).roundToInt(),
                    suggestedPlaces = it.suggestedPlaces,
                    focusedSuggestedPlace = it.focusedSuggestedPlace,
                    showLocationError = it.showLocationError
                )
            }
        }
    }

    fun getAiSuggestedPlaces(userLocation: LatLng?) {
        if (userLocation == null) {
            _uiState.update { 
                it.copy(
                    isLoading = it.isLoading,
                    isPathMode = it.isPathMode,
                    allLocations = it.allLocations,
                    visibleLocations = it.visibleLocations,
                    selectedLocations = it.selectedLocations,
                    activeFilter = it.activeFilter,
                    focusedMarker = it.focusedMarker,
                    optimalRoute = it.optimalRoute,
                    routeDistanceMeters = it.routeDistanceMeters,
                    routeDurationMinutes = it.routeDurationMinutes,
                    suggestedPlaces = it.suggestedPlaces,
                    focusedSuggestedPlace = null, // Clear focused AI place
                    showLocationError = true
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                isPathMode = it.isPathMode,
                allLocations = it.allLocations,
                visibleLocations = it.visibleLocations,
                selectedLocations = it.selectedLocations,
                activeFilter = it.activeFilter,
                focusedMarker = null, // Clear focused marker too
                optimalRoute = it.optimalRoute,
                routeDistanceMeters = it.routeDistanceMeters,
                routeDurationMinutes = it.routeDurationMinutes,
                suggestedPlaces = persistentListOf(),
                focusedSuggestedPlace = null, 
                showLocationError = false
            )
        }

        viewModelScope.launch {
            getSuggestedPlacesUseCase(userLocation.latitude, userLocation.longitude)
                .onSuccess { places ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPathMode = it.isPathMode,
                            allLocations = it.allLocations,
                            visibleLocations = it.visibleLocations,
                            selectedLocations = it.selectedLocations,
                            activeFilter = it.activeFilter,
                            focusedMarker = it.focusedMarker,
                            optimalRoute = it.optimalRoute,
                            routeDistanceMeters = it.routeDistanceMeters,
                            routeDurationMinutes = it.routeDurationMinutes,
                            suggestedPlaces = places.toPersistentList(),
                            focusedSuggestedPlace = it.focusedSuggestedPlace,
                            showLocationError = false
                        )
                    }
                }
                .onFailure { exception ->
                    Timber.e(exception, "Failed to get AI suggested places")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPathMode = it.isPathMode,
                            allLocations = it.allLocations,
                            visibleLocations = it.visibleLocations,
                            selectedLocations = it.selectedLocations,
                            activeFilter = it.activeFilter,
                            focusedMarker = it.focusedMarker,
                            optimalRoute = it.optimalRoute,
                            routeDistanceMeters = it.routeDistanceMeters,
                            routeDurationMinutes = it.routeDurationMinutes,
                            suggestedPlaces = it.suggestedPlaces,
                            focusedSuggestedPlace = it.focusedSuggestedPlace,
                            showLocationError = true
                        )
                    }
                }
        }
    }

    fun clearLocationError() {
        _uiState.update { it.copy(showLocationError = false, isLoading = it.isLoading, isPathMode = it.isPathMode, allLocations = it.allLocations, visibleLocations = it.visibleLocations, selectedLocations = it.selectedLocations, activeFilter = it.activeFilter, focusedMarker = it.focusedMarker, optimalRoute = it.optimalRoute, routeDistanceMeters = it.routeDistanceMeters, routeDurationMinutes = it.routeDurationMinutes, suggestedPlaces = it.suggestedPlaces, focusedSuggestedPlace = it.focusedSuggestedPlace) }
    }

    fun clearSuggestedPlaces() {
        _uiState.update { it.copy(suggestedPlaces = persistentListOf(), focusedSuggestedPlace = null, focusedMarker = null, isLoading = it.isLoading, isPathMode = it.isPathMode, allLocations = it.allLocations, visibleLocations = it.visibleLocations, selectedLocations = it.selectedLocations, activeFilter = it.activeFilter, optimalRoute = it.optimalRoute, routeDistanceMeters = it.routeDistanceMeters, routeDurationMinutes = it.routeDurationMinutes, showLocationError = it.showLocationError) }
    }
}
