package se.onemanstudio.playaroundwithai.feature.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.feature.maps.MapConstants.STOCKHOLM_LAT
import se.onemanstudio.playaroundwithai.feature.maps.MapConstants.STOCKHOLM_LNG
import se.onemanstudio.playaroundwithai.feature.maps.models.MapItemUiModel
import se.onemanstudio.playaroundwithai.feature.maps.states.MapError
import se.onemanstudio.playaroundwithai.feature.maps.states.MapUiState
import se.onemanstudio.playaroundwithai.feature.maps.states.SuggestedPlacesError
import se.onemanstudio.playaroundwithai.feature.maps.views.CustomMarkerIcon
import se.onemanstudio.playaroundwithai.feature.maps.views.FilterChip
import se.onemanstudio.playaroundwithai.feature.maps.views.MarkerInfoCard
import se.onemanstudio.playaroundwithai.feature.maps.views.PathModeBar
import se.onemanstudio.playaroundwithai.feature.maps.views.SideControls
import se.onemanstudio.playaroundwithai.feature.maps.views.SuggestedPlaceInfoCard
import se.onemanstudio.playaroundwithai.feature.map.R as MapFeatureR

private const val CAMERA_PADDING = 150

@SuppressLint("MissingPermission", "GoogleMapComposable")
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val view = LocalView.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val currentLoadingMessage by viewModel.currentLoadingMessage.collectAsState()

    val stockholm = LatLng(STOCKHOLM_LAT, STOCKHOLM_LNG)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(stockholm, MapConstants.MAX_ZOOM_LEVEL)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var permissionChecked by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var dataLoaded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        permissionChecked = true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(permissionChecked) {
        if (!permissionChecked) return@LaunchedEffect

        if (hasLocationPermission) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let { userLocation = LatLng(it.latitude, it.longitude) }
                    if (!dataLoaded) {
                        val lat = userLocation?.latitude ?: STOCKHOLM_LAT
                        val lng = userLocation?.longitude ?: STOCKHOLM_LNG
                        viewModel.loadMapData(lat, lng)
                        dataLoaded = true
                    }
                }
                .addOnFailureListener {
                    if (!dataLoaded) {
                        viewModel.loadMapData(STOCKHOLM_LAT, STOCKHOLM_LNG)
                        dataLoaded = true
                    }
                }
        } else {
            if (!dataLoaded) {
                viewModel.loadMapData(STOCKHOLM_LAT, STOCKHOLM_LNG)
                dataLoaded = true
            }
        }
    }

    LaunchedEffect(uiState.visibleLocations) {
        val allPoints = mutableListOf<LatLng>()
        uiState.visibleLocations.forEach { allPoints.add(it.position) }

        if (allPoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            allPoints.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), CAMERA_PADDING),
                durationMs = MapConstants.MOVE_TO_POINT_DURATION
            )
        }
    }

    LaunchedEffect(uiState.suggestedPlaces) {
        if (uiState.suggestedPlaces.isNotEmpty()) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    LaunchedEffect(uiState.optimalRoute) {
        if (uiState.optimalRoute.isNotEmpty()) {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            val boundsBuilder = LatLngBounds.builder()
            uiState.optimalRoute.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), CAMERA_PADDING),
                durationMs = MapConstants.MOVE_TO_POINT_DURATION
            )
        }
    }

    val locationErrorMessage = stringResource(MapFeatureR.string.ai_places_location_error)
    val fetchErrorMessage = stringResource(MapFeatureR.string.ai_places_fetch_error)
    val dismissLabel = stringResource(MapFeatureR.string.dismiss)

    LaunchedEffect(uiState.suggestedPlacesError) {
        val error = uiState.suggestedPlacesError ?: return@LaunchedEffect
        val message = when (error) {
            is SuggestedPlacesError.LocationUnavailable -> locationErrorMessage
            is SuggestedPlacesError.FetchFailed -> fetchErrorMessage
        }
        val result = snackbarHostState.showSnackbar(message = message, actionLabel = dismissLabel)
        if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
            viewModel.dismissSuggestedPlacesError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    if (isSystemInDarkTheme()) {
                        MapFeatureR.raw.custom_map_style_dark
                    } else {
                        MapFeatureR.raw.custom_map_style_light
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
            if (uiState.optimalRoute.isNotEmpty()) {
                @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                Polyline(
                    points = uiState.optimalRoute,
                    color = MaterialTheme.colorScheme.onSurface,
                    width = 12f,
                    geodesic = true
                )
            }

            uiState.visibleLocations.forEach { item ->
                key(item.id) {
                    val isSelected = uiState.selectedLocations.any { it.id == item.id } || uiState.focusedMarker?.id == item.id

                    val icon =
                        if (item.type == VehicleType.BICYCLE) Icons.AutoMirrored.Filled.DirectionsBike else Icons.Default.ElectricScooter

                    MarkerComposable(
                        keys = arrayOf<Any>(item.id, isSelected),
                        state = rememberUpdatedMarkerState(position = item.position),
                        title = item.name,
                        zIndex = if (isSelected) 10f else 1f,
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLng(item.position),
                                    durationMs = MapConstants.MOVE_TO_POINT_DURATION
                                )
                            }

                            if (uiState.isPathMode) {
                                viewModel.toggleSelection(item)
                            } else {
                                viewModel.selectMarker(item)
                            }
                            true
                        }
                    ) {
                        CustomMarkerIcon(
                            icon,
                            stringResource(id = MapFeatureR.string.marker_content_description, item.name),
                            isSelected
                        )
                    }
                }
            }

            uiState.suggestedPlaces.forEach { place ->
                key(place.name + place.lat + place.lng) {
                    MarkerComposable(
                        state = rememberUpdatedMarkerState(position = LatLng(place.lat, place.lng)),
                        title = place.name,
                        snippet = place.description,
                        onClick = {
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLng(LatLng(place.lat, place.lng)),
                                    durationMs = MapConstants.MOVE_TO_POINT_DURATION
                                )
                            }
                            viewModel.selectSuggestedPlace(place)
                            true
                        }
                    ) {
                        CustomMarkerIcon(
                            Icons.Filled.Stars,
                            stringResource(id = MapFeatureR.string.ai_suggested_place_marker_content_description, place.name),
                            false
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

        MapControls(
            uiState = uiState,
            cameraPositionState = cameraPositionState,
            onMyLocationClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val userLatLng = LatLng(it.latitude, it.longitude)
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        userLatLng,
                                        MapConstants.MAX_ZOOM_LEVEL
                                    ),
                                    durationMs = MapConstants.MOVE_TO_POINT_DURATION
                                )
                            }
                        }
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            onSetPathMode = { isCurrentlyPathMode -> viewModel.setPathMode(!isCurrentlyPathMode) },
        )

        PathModePanel(
            isVisible = uiState.isPathMode,
            selectedCount = uiState.selectedLocations.size,
            distanceMeters = uiState.routeDistanceMeters,
            durationMinutes = uiState.routeDurationMinutes,
            onGoClick = {
                if (userLocation != null) {
                    viewModel.calculateOptimalRoute(userLocation)
                }
            },
        )

        MarkerInfoPanel(
            marker = uiState.focusedMarker,
            isPathMode = uiState.isPathMode,
            onClose = { viewModel.selectMarker(null) },
        )

        SuggestedPlaceInfoPanel(
            place = uiState.focusedSuggestedPlace,
            isPathMode = uiState.isPathMode,
            onClose = { viewModel.selectSuggestedPlace(null) },
        )

        SnackbarContainer(snackbarHostState)

        LoadingState(
            isLoading = uiState.isLoading,
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

@Composable
private fun BoxScope.MapControls(
    uiState: MapUiState,
    cameraPositionState: CameraPositionState,
    onMyLocationClick: () -> Unit,
    onSetPathMode: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(Dimensions.paddingLarge)
    ) {
        SideControls(
            uiState = uiState,
            cameraPositionState = cameraPositionState,
            onMyLocationClick = onMyLocationClick,
            onSetPathMode = onSetPathMode,
        )
    }
}

@Composable
private fun BoxScope.TopActions(
    uiState: MapUiState,
    onToggleFilter: (VehicleType) -> Unit,
    onSuggestPlaces: () -> Unit,
) {
    val view = LocalView.current

    AnimatedVisibility(
        visible = !uiState.isPathMode,
        enter = slideInHorizontally(
            initialOffsetX = { -it * 2 },
            animationSpec = tween(
                durationMillis = AnimationConstants.ANIMATION_DURATION,
                easing = EaseInOutQuart
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it * 2 },
            animationSpec = tween(
                durationMillis = AnimationConstants.ANIMATION_DURATION,
                easing = EaseInOutQuart
            )
        ),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = Dimensions.paddingMedium)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                text = stringResource(id = MapFeatureR.string.scooters_filter_chip_label),
                selected = uiState.activeFilter.contains(VehicleType.SCOOTER)
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onToggleFilter(VehicleType.SCOOTER)
            }

            FilterChip(
                text = stringResource(id = MapFeatureR.string.bicycles_filter_chip_label),
                selected = uiState.activeFilter.contains(VehicleType.BICYCLE)
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onToggleFilter(VehicleType.BICYCLE)
            }

            NeoBrutalIconButton(
                backgroundColor = energeticOrange,
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = stringResource(id = MapFeatureR.string.ai_suggest_button_content_description),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onSuggestPlaces()
                },
            )
        }
    }
}

@Composable
private fun BoxScope.PathModePanel(
    isVisible: Boolean,
    selectedCount: Int,
    distanceMeters: Int,
    durationMinutes: Int,
    onGoClick: () -> Unit,
) {
    AnimatedVisibility(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(Dimensions.paddingMedium),
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.ANIMATION_DURATION,
                easing = EaseInOutQuart
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.ANIMATION_DURATION,
                easing = EaseInOutQuart
            )
        )
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(Dimensions.paddingLarge)
        ) {
            PathModeBar(
                count = selectedCount,
                distance = distanceMeters,
                duration = durationMinutes,
                onGoClick = onGoClick,
            )
        }
    }
}

@Composable
private fun BoxScope.MarkerInfoPanel(
    marker: MapItemUiModel?,
    isPathMode: Boolean,
    onClose: () -> Unit,
) {
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = marker != null && !isPathMode,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.ANIMATION_DURATION,
                easing = EaseInOutQuart
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.ANIMATION_DURATION,
                easing = EaseInOutQuart
            )
        )
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(Dimensions.paddingLarge)
        ) {
            marker?.let {
                MarkerInfoCard(
                    marker = it,
                    onClose = onClose,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SuggestedPlaceInfoPanel(
    place: SuggestedPlace?,
    isPathMode: Boolean,
    onClose: () -> Unit,
) {
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visible = place != null && !isPathMode,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.ANIMATION_DURATION,
                easing = EaseInOutQuart
            )
        ) + fadeIn(animationSpec = tween(durationMillis = AnimationConstants.ANIMATION_DURATION)),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.ANIMATION_DURATION,
                easing = EaseInOutQuart
            )
        ) + fadeOut(animationSpec = tween(durationMillis = AnimationConstants.ANIMATION_DURATION)),
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(Dimensions.paddingLarge)
        ) {
            place?.let {
                SuggestedPlaceInfoCard(
                    place = it,
                    onClose = onClose,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.SnackbarContainer(snackbarHostState: SnackbarHostState) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(Dimensions.paddingMedium),
        snackbar = { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                actionColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    )
}

@Composable
private fun ErrorState(
    error: MapError?,
    onRetry: () -> Unit
) {
    error?.let { error ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface.copy(alpha = Alphas.high)),
            contentAlignment = Alignment.Center
        ) {
            NeoBrutalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(Dimensions.paddingLarge),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(Dimensions.paddingLarge)
                ) {
                    Icon(
                        imageVector = when (error) {
                            is MapError.NetworkError -> Icons.Rounded.WifiOff
                            is MapError.Unknown -> Icons.Rounded.Warning
                        },
                        contentDescription = stringResource(MapFeatureR.string.map_error_icon),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimensions.paddingExtraLarge)
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                    Text(
                        text = stringResource(MapFeatureR.string.map_error_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                    Text(
                        text = when (error) {
                            is MapError.NetworkError -> stringResource(MapFeatureR.string.map_error_network)
                            is MapError.Unknown -> error.message ?: stringResource(MapFeatureR.string.map_error_unknown)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                    NeoBrutalButton(
                        text = stringResource(MapFeatureR.string.map_error_retry),
                        onClick = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState(
    isLoading: Boolean,
    currentLoadingMessage: String
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface.copy(alpha = Alphas.high)),
            contentAlignment = Alignment.Center,
        ) {
            NeoBrutalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(Dimensions.paddingLarge),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(Dimensions.paddingLarge)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.wrapContentSize(),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (currentLoadingMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                        AnimatedVisibility(
                            visible = currentLoadingMessage.isNotEmpty(),
                            enter = fadeIn(animationSpec = tween(durationMillis = AnimationConstants.ANIMATION_DURATION)),
                            exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.ANIMATION_DURATION))
                        ) {
                            Text(
                                text = currentLoadingMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = Dimensions.paddingLarge)
                            )
                        }
                    }
                }
            }
        }
    }
}
