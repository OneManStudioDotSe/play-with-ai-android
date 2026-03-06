@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButtonSmall
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.feature.explore.ExploreConstants.STOCKHOLM_LAT
import se.onemanstudio.playaroundwithai.feature.explore.ExploreConstants.STOCKHOLM_LNG
import se.onemanstudio.playaroundwithai.feature.explore.states.SuggestedPlacesError
import se.onemanstudio.playaroundwithai.feature.explore.views.CustomMarkerIcon
import se.onemanstudio.playaroundwithai.feature.explore.views.ErrorState
import se.onemanstudio.playaroundwithai.feature.explore.views.ExploreControls
import se.onemanstudio.playaroundwithai.feature.explore.views.LoadingState
import se.onemanstudio.playaroundwithai.feature.explore.views.MarkerInfoPanel
import se.onemanstudio.playaroundwithai.feature.explore.views.PathModeHint
import se.onemanstudio.playaroundwithai.feature.explore.views.PathModePanel
import se.onemanstudio.playaroundwithai.feature.explore.views.SnackbarContainer
import se.onemanstudio.playaroundwithai.feature.explore.views.SuggestedPlaceInfoPanel
import se.onemanstudio.playaroundwithai.feature.explore.views.TopActions
import se.onemanstudio.playaroundwithai.feature.explore.R as ExploreFeatureR

private const val CAMERA_PADDING = 150

private sealed interface LocationState {
    object Pending : LocationState
    object Denied : LocationState
    data class Ready(val location: LatLng) : LocationState
}

@SuppressLint("MissingPermission", "GoogleMapComposable")
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    settingsContent: @Composable (() -> Unit) -> Unit = { _ -> },
) {
    val context = LocalContext.current
    val view = LocalView.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentLoadingMessage = uiState.suggestions.loadingMessageResId
        ?.let { stringResource(it) }
        ?: ""

    val stockholm = LatLng(STOCKHOLM_LAT, STOCKHOLM_LNG)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(stockholm, ExploreConstants.MAX_ZOOM_LEVEL)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    var locationState by remember { mutableStateOf<LocationState>(LocationState.Pending) }
    var cameraSettled by remember { mutableStateOf(false) }
    var dataLoaded by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val hasLocationPermission = locationState !is LocationState.Pending && locationState !is LocationState.Denied
    val userLocation = (locationState as? LocationState.Ready)?.location

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    locationState = if (location != null) {
                        LocationState.Ready(LatLng(location.latitude, location.longitude))
                    } else {
                        LocationState.Denied
                    }
                }
                .addOnFailureListener {
                    locationState = LocationState.Denied
                }
        } else {
            locationState = LocationState.Denied
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(locationState) {
        when (val state = locationState) {
            is LocationState.Ready -> {
                scope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(state.location, ExploreConstants.MAX_ZOOM_LEVEL),
                        durationMs = ExploreConstants.MOVE_TO_POINT_DURATION
                    )
                }
            }
            is LocationState.Denied -> cameraSettled = true
            is LocationState.Pending -> Unit
        }
    }

    // Watch camera movement — when it stops moving after animating, mark as settled
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && !cameraSettled) {
            cameraSettled = true
        }
    }

    // Once camera has settled, load map data
    LaunchedEffect(cameraSettled) {
        if (cameraSettled && !dataLoaded) {
            val lat = userLocation?.latitude ?: STOCKHOLM_LAT
            val lng = userLocation?.longitude ?: STOCKHOLM_LNG
            viewModel.loadMapData(lat, lng)
            dataLoaded = true
        }
    }

    LaunchedEffect(uiState.markers.visibleLocations) {
        val allPoints = mutableListOf<LatLng>()
        uiState.markers.visibleLocations.forEach { allPoints.add(it.position) }

        if (allPoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            allPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), CAMERA_PADDING),
                durationMs = ExploreConstants.MOVE_TO_POINT_DURATION
            )
        }
    }

    LaunchedEffect(uiState.suggestions.places) {
        if (uiState.suggestions.places.isNotEmpty()) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    LaunchedEffect(uiState.pathMode.optimalRoute) {
        if (uiState.pathMode.optimalRoute.isNotEmpty()) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            val boundsBuilder = LatLngBounds.builder()
            uiState.pathMode.optimalRoute.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), CAMERA_PADDING),
                durationMs = ExploreConstants.MOVE_TO_POINT_DURATION
            )
        }
    }

    val locationErrorMessage = stringResource(ExploreFeatureR.string.ai_places_location_error)
    val fetchErrorMessage = stringResource(ExploreFeatureR.string.ai_places_fetch_error)
    val dismissLabel = stringResource(ExploreFeatureR.string.dismiss)

    LaunchedEffect(uiState.suggestions.error) {
        val error = uiState.suggestions.error ?: return@LaunchedEffect
        val message = when (error) {
            is SuggestedPlacesError.LocationUnavailable -> locationErrorMessage
            is SuggestedPlacesError.FetchFailed -> fetchErrorMessage
        }
        val result = snackbarHostState.showSnackbar(message = message, actionLabel = dismissLabel)
        if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
            viewModel.dismissSuggestedPlacesError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoBrutalTopAppBar(
                title = stringResource(ExploreFeatureR.string.explore_title),
                actions = {
                    NeoBrutalIconButtonSmall(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(
                            se.onemanstudio.playaroundwithai.core.ui.views.R.string.settings_icon_description
                        ),
                        onClick = { showSettings = true },
                    )
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                        context,
                        if (isSystemInDarkTheme()) {
                            ExploreFeatureR.raw.custom_map_style_dark
                        } else {
                            ExploreFeatureR.raw.custom_map_style_light
                        }
                    ),
                    isMyLocationEnabled = hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    compassEnabled = false,
                    indoorLevelPickerEnabled = false,
                    mapToolbarEnabled = false,
                    rotationGesturesEnabled = true,
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = true,
                    myLocationButtonEnabled = false,
                ),
                onMapClick = {
                    viewModel.selectMarker(null)
                    viewModel.selectSuggestedPlace(null)
                }
            ) {
                if (uiState.pathMode.optimalRoute.isNotEmpty()) {
                    @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                    Polyline(
                        points = uiState.pathMode.optimalRoute,
                        color = MaterialTheme.colorScheme.onSurface,
                        width = 12f,
                        geodesic = true
                    )
                }

                uiState.markers.visibleLocations.forEach { item ->
                    key(item.id) {
                        val isSelected = uiState.pathMode.selectedLocations.any { it.id == item.id } || uiState.markers.focusedMarker?.id == item.id

                        val icon =
                            if (item.type == VehicleType.Bicycle) Icons.AutoMirrored.Filled.DirectionsBike else Icons.Default.ElectricScooter

                        MarkerComposable(
                            keys = arrayOf<Any>(item.id, isSelected),
                            state = rememberUpdatedMarkerState(position = item.position),
                            title = item.name,
                            zIndex = if (isSelected) 10f else 1f,
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLng(item.position),
                                        durationMs = ExploreConstants.MOVE_TO_POINT_DURATION
                                    )
                                }

                                if (uiState.pathMode.isActive) {
                                    viewModel.toggleSelection(item)
                                } else {
                                    viewModel.selectMarker(item)
                                }
                                true
                            }
                        ) {
                            CustomMarkerIcon(
                                icon,
                                stringResource(id = ExploreFeatureR.string.marker_content_description, item.name),
                                isSelected
                            )
                        }
                    }
                }

                uiState.suggestions.places.forEach { place ->
                    key(place.name + place.lat + place.lng) {
                        val syntheticId = "suggested_${place.name}_${place.lat}_${place.lng}"
                        val isSelected = uiState.suggestions.focusedPlace == place ||
                                uiState.pathMode.selectedLocations.any { it.id == syntheticId }

                        MarkerComposable(
                            keys = arrayOf<Any>(place.name, place.lat, place.lng, isSelected),
                            state = rememberUpdatedMarkerState(position = LatLng(place.lat, place.lng)),
                            title = place.name,
                            snippet = place.description,
                            zIndex = if (isSelected) 10f else 2f,
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLng(LatLng(place.lat, place.lng)),
                                        durationMs = ExploreConstants.MOVE_TO_POINT_DURATION
                                    )
                                }
                                if (uiState.pathMode.isActive) {
                                    viewModel.toggleSuggestedPlaceSelection(place)
                                } else {
                                    viewModel.selectSuggestedPlace(place)
                                }
                                true
                            }
                        ) {
                            CustomMarkerIcon(
                                Icons.Filled.Stars,
                                stringResource(id = ExploreFeatureR.string.ai_suggested_place_marker_content_description, place.name),
                                isSelected
                            )
                        }
                    }
                }
            }

            TopActions(
                uiState = uiState,
                onToggleFilter = { type -> viewModel.toggleFilter(type) },
                onSuggestPlaces = { viewModel.getAiSuggestedPlaces(userLocation) },
            )

            PathModeHint(isVisible = uiState.pathMode.isActive)

            ExploreControls(
                uiState = uiState,
                cameraPositionState = cameraPositionState,
                onMyLocationClick = {
                    val currentPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (currentPermission) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val loc = LatLng(it.latitude, it.longitude)
                                locationState = LocationState.Ready(loc)
                            }
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                onSetPathMode = { isCurrentlyPathMode -> viewModel.setPathMode(!isCurrentlyPathMode) },
            )

            PathModePanel(
                isVisible = uiState.pathMode.isActive,
                selectedCount = uiState.pathMode.selectedLocations.size,
                distanceMeters = uiState.pathMode.routeDistanceMeters,
                durationMinutes = uiState.pathMode.routeDurationMinutes,
                onGoClick = {
                    if (userLocation != null) {
                        viewModel.calculateOptimalRoute(userLocation)
                    }
                },
            )

            MarkerInfoPanel(
                marker = uiState.markers.focusedMarker,
                isPathMode = uiState.pathMode.isActive,
                onClose = { viewModel.selectMarker(null) },
            )

            SuggestedPlaceInfoPanel(
                place = uiState.suggestions.focusedPlace,
                isPathMode = uiState.pathMode.isActive,
                onClose = { viewModel.selectSuggestedPlace(null) },
            )

            SnackbarContainer(snackbarHostState)

            LoadingState(
                isLoading = uiState.suggestions.isLoading,
                currentLoadingMessage = currentLoadingMessage
            )

            ErrorState(
                error = uiState.error,
                onRetry = {
                    val lat = userLocation?.latitude ?: STOCKHOLM_LAT
                    val lng = userLocation?.longitude ?: STOCKHOLM_LNG
                    viewModel.loadMapData(lat, lng)
                }
            )
        }
    }

    if (showSettings) {
        settingsContent { showSettings = false }
    }
}
