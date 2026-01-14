package se.onemanstudio.playaroundwithai.feature.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.core.domain.usecase.GetMapItemsUseCase
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel
import se.onemanstudio.playaroundwithai.feature.maps.models.toUiModel
import se.onemanstudio.playaroundwithai.feature.maps.state.MapUiState
import se.onemanstudio.playaroundwithai.feature.maps.utils.calculatePathDistance
import se.onemanstudio.playaroundwithai.feature.maps.utils.permutations
import javax.inject.Inject
import kotlin.math.roundToInt

private const val AMOUNT_OF_POINTS_TO_GENERATE = 30
private const val WALKING_SPEED_METERS_PER_MIN = 83.0 // approx 5km/h

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMapItemsUseCase: GetMapItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        loadMapData()
    }

    fun loadMapData() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val data = getMapItemsUseCase(AMOUNT_OF_POINTS_TO_GENERATE).map { it.toUiModel() }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allLocations = data,
                    visibleLocations = data
                )
            }
        }
    }

    fun setPathMode(active: Boolean) {
        _uiState.update {
            it.copy(
                isPathMode = active,
                focusedMarker = null,
                selectedLocations = emptyList(),
                optimalRoute = emptyList()
            )
        }
    }

    fun selectMarker(marker: MapItemUiModel?) {
        _uiState.update { it.copy(focusedMarker = marker) }
    }

    fun toggleFilter(type: VehicleType) {
        _uiState.update { currentState ->
            val newFilters = if (currentState.activeFilter.contains(type)) {
                currentState.activeFilter - type
            } else {
                currentState.activeFilter + type
            }

            val filtered = currentState.allLocations.filter { newFilters.contains(it.type) }

            currentState.copy(
                activeFilter = newFilters,
                visibleLocations = filtered,
                selectedLocations = emptyList(),
                optimalRoute = emptyList(),
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
                currentSelected.filter { it.id != location.id }
            } else {
                if (currentSelected.size < MapConstants.MAX_SELECTABLE_POINTS) {
                    currentSelected + location.copy(isSelected = true)
                } else {
                    currentSelected // Limit reached, do not add
                }
            }
            state.copy(selectedLocations = newSelected, optimalRoute = emptyList(), routeDistanceMeters = 0)
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

        val fullPath = listOf(startPoint) + bestPermutation
        val totalDistance = calculatePathDistance(startPoint, bestPermutation)

        _uiState.update {
            it.copy(
                optimalRoute = fullPath,
                routeDistanceMeters = (totalDistance * 1000).roundToInt(),
                routeDurationMinutes = ((totalDistance * 1000) / WALKING_SPEED_METERS_PER_MIN).roundToInt()
            )
        }
    }
}
