package se.onemanstudio.playaroundwithai.feature.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase.GetMapItemsUseCase
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel
import se.onemanstudio.playaroundwithai.feature.maps.models.toUiModel
import se.onemanstudio.playaroundwithai.feature.maps.states.MapError
import se.onemanstudio.playaroundwithai.feature.maps.states.MapUiState
import se.onemanstudio.playaroundwithai.feature.maps.utils.calculatePathDistance
import se.onemanstudio.playaroundwithai.feature.maps.utils.permutations
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt

private const val AMOUNT_OF_POINTS_TO_GENERATE = 30
private const val WALKING_SPEED_METERS_PER_MIN = 83.0 // approx 5km/h

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMapItemsUseCase: GetMapItemsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMapData()
    }

    @Suppress("TooGenericExceptionCaught")
    fun loadMapData() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val data = getMapItemsUseCase(AMOUNT_OF_POINTS_TO_GENERATE).map { it.toUiModel() }.toPersistentList()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allLocations = data,
                        visibleLocations = data
                    )
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

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setPathMode(active: Boolean) {
        _uiState.update {
            it.copy(
                isPathMode = active,
                focusedMarker = null,
                selectedLocations = persistentListOf(),
                optimalRoute = persistentListOf(),
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
}
