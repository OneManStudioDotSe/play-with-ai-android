package se.onemanstudio.playaroundwithai.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.data.explore.data.settings.ExploreSettingsHolder
import se.onemanstudio.playaroundwithai.data.explore.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.data.explore.domain.model.toExploreItem
import se.onemanstudio.playaroundwithai.data.explore.domain.usecase.GetExploreItemsUseCase
import se.onemanstudio.playaroundwithai.data.explore.domain.usecase.GetSuggestedPlacesUseCase
import se.onemanstudio.playaroundwithai.core.network.monitor.NetworkMonitor
import se.onemanstudio.playaroundwithai.feature.explore.models.ExploreItemUiModel
import se.onemanstudio.playaroundwithai.feature.explore.models.toUiModel
import se.onemanstudio.playaroundwithai.feature.explore.states.ExploreError
import se.onemanstudio.playaroundwithai.feature.explore.states.ExploreUiState
import se.onemanstudio.playaroundwithai.feature.explore.states.MarkersState
import se.onemanstudio.playaroundwithai.feature.explore.states.PathModeState
import se.onemanstudio.playaroundwithai.feature.explore.states.SuggestedPlacesError
import se.onemanstudio.playaroundwithai.feature.explore.states.SuggestionsState
import se.onemanstudio.playaroundwithai.feature.explore.usecase.CalculateOptimalRouteUseCase
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

private const val LOADING_MESSAGE_DURATION = 3000L

private val LOADING_MESSAGE_RES_IDS = listOf(
    R.string.loading_message_1,
    R.string.loading_message_2,
    R.string.loading_message_3,
    R.string.loading_message_4,
    R.string.loading_message_5,
)

@Suppress("TooManyFunctions")
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getExploreItemsUseCase: GetExploreItemsUseCase,
    private val getSuggestedPlacesUseCase: GetSuggestedPlacesUseCase,
    private val calculateOptimalRouteUseCase: CalculateOptimalRouteUseCase,
    private val apiKeyAvailability: ApiKeyAvailability,
    private val networkMonitor: NetworkMonitor,
    private val exploreSettingsHolder: ExploreSettingsHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState = _uiState.asStateFlow()

    private var loadingMessageJob: Job? = null
    private var lastCenterLat: Double? = null
    private var lastCenterLng: Double? = null

    init {
        observeSettingsChanges()
    }

    private fun observeSettingsChanges() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                exploreSettingsHolder.vehicleCount,
                exploreSettingsHolder.searchRadiusKm,
            ) { _, _ -> }
                .drop(1)
                .collect {
                    val lat = lastCenterLat ?: return@collect
                    val lng = lastCenterLng ?: return@collect
                    loadMapData(lat, lng)
                }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun loadMapData(centerLat: Double, centerLng: Double) {
        lastCenterLat = centerLat
        lastCenterLng = centerLng
        if (!apiKeyAvailability.isMapsKeyAvailable) {
            _uiState.update { it.copy(markers = it.markers.copy(isLoading = false), error = ExploreError.ApiKeyMissing) }
            return
        }

        _uiState.update { it.copy(markers = it.markers.copy(isLoading = true), error = null) }

        if (!networkMonitor.isNetworkAvailable()) {
            Timber.w("ExploreViewModel - No network available, cannot load map data")
            _uiState.update { it.copy(markers = it.markers.copy(isLoading = false), error = ExploreError.NetworkError) }
            return
        }

        viewModelScope.launch {
            try {
                val data = getExploreItemsUseCase(exploreSettingsHolder.vehicleCount.value, centerLat, centerLng)
                    .map { it.toUiModel() }.toPersistentList()
                _uiState.update {
                    it.copy(markers = it.markers.copy(isLoading = false, allLocations = data, visibleLocations = data))
                }
            } catch (e: IOException) {
                Timber.e(e, "ExploreViewModel - Failed to load map data (network)")
                _uiState.update { it.copy(markers = it.markers.copy(isLoading = false), error = ExploreError.NetworkError) }
            } catch (e: Exception) {
                Timber.e(e, "ExploreViewModel - Failed to load map data")
                _uiState.update { it.copy(markers = it.markers.copy(isLoading = false), error = ExploreError.Unknown(e.localizedMessage)) }
            }
        }
    }

    fun setPathMode(active: Boolean) {
        _uiState.update {
            it.copy(
                markers = it.markers.copy(focusedMarker = null),
                pathMode = PathModeState(isActive = active),
                suggestions = it.suggestions.copy(focusedPlace = null),
            )
        }
    }

    fun selectMarker(marker: ExploreItemUiModel?) {
        _uiState.update {
            it.copy(
                markers = it.markers.copy(focusedMarker = marker),
                suggestions = it.suggestions.copy(focusedPlace = null),
            )
        }
    }

    fun toggleFilter(type: VehicleType) {
        _uiState.update { currentState ->
            val newFilters = if (currentState.markers.activeFilter.contains(type)) {
                currentState.markers.activeFilter - type
            } else {
                currentState.markers.activeFilter + type
            }

            val filtered = currentState.markers.allLocations
                .filter { newFilters.contains(it.type) }
                .toPersistentList()

            currentState.copy(
                markers = currentState.markers.copy(
                    activeFilter = newFilters,
                    visibleLocations = filtered,
                    focusedMarker = null,
                ),
                pathMode = PathModeState(isActive = currentState.pathMode.isActive),
            )
        }
    }

    fun toggleSelection(location: ExploreItemUiModel) {
        if (!_uiState.value.pathMode.isActive) return

        _uiState.update { state ->
            val currentSelected = state.pathMode.selectedLocations
            val isAlreadySelected = currentSelected.any { it.id == location.id }

            val newSelected = if (isAlreadySelected) {
                currentSelected.filter { it.id != location.id }.toPersistentList()
            } else {
                if (currentSelected.size < ExploreConstants.MAX_SELECTABLE_POINTS) {
                    (currentSelected + location.copy(isSelected = true)).toPersistentList()
                } else {
                    currentSelected
                }
            }
            state.copy(
                pathMode = state.pathMode.copy(
                    selectedLocations = newSelected,
                    optimalRoute = persistentListOf(),
                    routeDistanceMeters = 0,
                )
            )
        }
    }

    fun toggleSuggestedPlaceSelection(place: SuggestedPlace) {
        if (!_uiState.value.pathMode.isActive) return

        val syntheticUiModel = ExploreItemUiModel(mapItem = place.toExploreItem(), isSelected = true)
        val syntheticId = syntheticUiModel.id

        _uiState.update { state ->
            val currentSelected = state.pathMode.selectedLocations
            val isAlreadySelected = currentSelected.any { it.id == syntheticId }

            val newSelected = if (isAlreadySelected) {
                currentSelected.filter { it.id != syntheticId }.toPersistentList()
            } else {
                if (currentSelected.size < ExploreConstants.MAX_SELECTABLE_POINTS) {
                    (currentSelected + syntheticUiModel).toPersistentList()
                } else {
                    currentSelected
                }
            }
            state.copy(
                pathMode = state.pathMode.copy(
                    selectedLocations = newSelected,
                    optimalRoute = persistentListOf(),
                    routeDistanceMeters = 0,
                )
            )
        }
    }

    fun calculateOptimalRoute(userLocation: LatLng?) {
        val points = _uiState.value.pathMode.selectedLocations.map { it.position }
        if (points.isEmpty()) return

        val startPoint = userLocation ?: points.first()
        val result = calculateOptimalRouteUseCase(startPoint, points)

        _uiState.update {
            it.copy(
                pathMode = it.pathMode.copy(
                    optimalRoute = result.orderedPath,
                    routeDistanceMeters = result.distanceMeters,
                    routeDurationMinutes = result.durationMinutes,
                )
            )
        }
    }

    fun getAiSuggestedPlaces(userLocation: LatLng?) {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _uiState.update { it.copy(suggestions = it.suggestions.copy(error = SuggestedPlacesError.FetchFailed)) }
            return
        }

        if (userLocation == null) {
            _uiState.update {
                it.copy(suggestions = it.suggestions.copy(focusedPlace = null, error = SuggestedPlacesError.LocationUnavailable))
            }
            return
        }

        _uiState.update {
            it.copy(
                markers = it.markers.copy(focusedMarker = null),
                suggestions = SuggestionsState(isLoading = true),
            )
        }
        startLoadingMessageCycle()

        viewModelScope.launch {
            getSuggestedPlacesUseCase(userLocation.latitude, userLocation.longitude)
                .onSuccess { places ->
                    stopLoadingMessageCycle()
                    _uiState.update {
                        it.copy(suggestions = it.suggestions.copy(isLoading = false, places = places.toPersistentList(), error = null))
                    }
                }
                .onFailure { exception ->
                    Timber.e(exception, "Failed to get AI suggested places")
                    stopLoadingMessageCycle()
                    _uiState.update {
                        it.copy(suggestions = it.suggestions.copy(isLoading = false, error = SuggestedPlacesError.FetchFailed))
                    }
                }
        }
    }

    fun dismissSuggestedPlacesError() {
        _uiState.update { it.copy(suggestions = it.suggestions.copy(error = null)) }
    }

    fun selectSuggestedPlace(place: SuggestedPlace?) {
        _uiState.update {
            it.copy(
                markers = it.markers.copy(focusedMarker = null),
                suggestions = it.suggestions.copy(focusedPlace = place),
            )
        }
    }

    private fun startLoadingMessageCycle() {
        loadingMessageJob?.cancel()
        loadingMessageJob = viewModelScope.launch {
            var index = 0
            while (isActive) {
                _uiState.update { it.copy(suggestions = it.suggestions.copy(loadingMessageResId = LOADING_MESSAGE_RES_IDS[index])) }
                delay(LOADING_MESSAGE_DURATION)
                index = (index + 1) % LOADING_MESSAGE_RES_IDS.size
            }
        }
    }

    private fun stopLoadingMessageCycle() {
        loadingMessageJob?.cancel()
        loadingMessageJob = null
    }
}
