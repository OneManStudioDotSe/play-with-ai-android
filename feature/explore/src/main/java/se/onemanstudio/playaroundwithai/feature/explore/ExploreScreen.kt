package se.onemanstudio.playaroundwithai.feature.explore

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
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
import se.onemanstudio.playaroundwithai.data.explore.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.feature.explore.ExploreConstants.STOCKHOLM_LAT
import se.onemanstudio.playaroundwithai.feature.explore.ExploreConstants.STOCKHOLM_LNG
import se.onemanstudio.playaroundwithai.feature.explore.models.ExploreItemUiModel
import se.onemanstudio.playaroundwithai.feature.explore.states.ExploreError
import se.onemanstudio.playaroundwithai.feature.explore.states.ExploreUiState
import se.onemanstudio.playaroundwithai.feature.explore.states.SuggestedPlacesError
import se.onemanstudio.playaroundwithai.feature.explore.views.CustomMarkerIcon
import se.onemanstudio.playaroundwithai.feature.explore.views.FilterChip
import se.onemanstudio.playaroundwithai.feature.explore.views.MarkerInfoCard
import se.onemanstudio.playaroundwithai.feature.explore.views.PathModeBar
import se.onemanstudio.playaroundwithai.feature.explore.views.SideControls
import se.onemanstudio.playaroundwithai.feature.explore.views.SuggestedPlaceInfoCard
import se.onemanstudio.playaroundwithai.feature.explore.R as ExploreFeatureR

private const val CAMERA_PADDING = 150

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

    val uiState by viewModel.uiState.collectAsState()
    val currentLoadingMessage = if (uiState.loadingMessageResId != 0) {
        stringResource(uiState.loadingMessageResId)
    } else {
        ""
    }

    val stockholm = LatLng(STOCKHOLM_LAT, STOCKHOLM_LNG)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(stockholm, ExploreConstants.MAX_ZOOM_LEVEL)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var permissionChecked by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var dataLoaded by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

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
                durationMs = ExploreConstants.MOVE_TO_POINT_DURATION
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
                durationMs = ExploreConstants.MOVE_TO_POINT_DURATION
            )
        }
    }

    val locationErrorMessage = stringResource(ExploreFeatureR.string.ai_places_location_error)
    val fetchErrorMessage = stringResource(ExploreFeatureR.string.ai_places_fetch_error)
    val dismissLabel = stringResource(ExploreFeatureR.string.dismiss)

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoBrutalTopAppBar(
                title = stringResource(ExploreFeatureR.string.explore_title),
                actions = {
                    NeoBrutalIconButton(
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
                            stringResource(id = ExploreFeatureR.string.marker_content_description, item.name),
                            isSelected
                        )
                    }
                }
            }

            uiState.suggestedPlaces.forEach { place ->
                key(place.name + place.lat + place.lng) {
                    val syntheticId = "suggested_${place.name}_${place.lat}_${place.lng}"
                    val isSelected = uiState.focusedSuggestedPlace == place ||
                        uiState.selectedLocations.any { it.id == syntheticId }

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
                            if (uiState.isPathMode) {
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

        PathModeHint(isVisible = uiState.isPathMode)

        ExploreControls(
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
                                        ExploreConstants.MAX_ZOOM_LEVEL
                                    ),
                                    durationMs = ExploreConstants.MOVE_TO_POINT_DURATION
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

    if (showSettings) {
        settingsContent { showSettings = false }
    }
}

@Composable
private fun BoxScope.ExploreControls(
    uiState: ExploreUiState,
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
    uiState: ExploreUiState,
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
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .padding(top = Dimensions.paddingMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                text = stringResource(id = ExploreFeatureR.string.scooters_filter_chip_label),
                selected = uiState.activeFilter.contains(VehicleType.Scooter)
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onToggleFilter(VehicleType.Scooter)
            }

            Spacer(modifier = Modifier.width(Dimensions.paddingLarge))

            FilterChip(
                text = stringResource(id = ExploreFeatureR.string.bicycles_filter_chip_label),
                selected = uiState.activeFilter.contains(VehicleType.Bicycle)
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onToggleFilter(VehicleType.Bicycle)
            }

            Spacer(modifier = Modifier.weight(1f))

            NeoBrutalIconButton(
                backgroundColor = energeticOrange,
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = stringResource(id = ExploreFeatureR.string.ai_suggest_button_content_description),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onSuggestPlaces()
                },
            )
        }
    }
}

@Composable
private fun BoxScope.PathModeHint(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = AnimationConstants.ANIMATION_DURATION)),
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.ANIMATION_DURATION)),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = Dimensions.paddingMedium)
    ) {
        NeoBrutalCard(
            modifier = Modifier.padding(horizontal = Dimensions.paddingLarge)
        ) {
            Text(
                text = stringResource(id = ExploreFeatureR.string.path_mode_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(Dimensions.paddingLarge)
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
    marker: ExploreItemUiModel?,
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
                .heightIn(max = 200.dp)
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
                .heightIn(max = 200.dp)
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
    error: ExploreError?,
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
                            is ExploreError.ApiKeyMissing -> Icons.Rounded.VpnKey
                            is ExploreError.NetworkError -> Icons.Rounded.WifiOff
                            is ExploreError.Unknown -> Icons.Rounded.Warning
                        },
                        contentDescription = stringResource(ExploreFeatureR.string.explore_error_icon),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimensions.iconSizeXLarge)
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                    Text(
                        text = stringResource(ExploreFeatureR.string.explore_error_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                    Text(
                        text = when (error) {
                            is ExploreError.ApiKeyMissing -> stringResource(ExploreFeatureR.string.error_maps_api_key_missing)
                            is ExploreError.NetworkError -> stringResource(ExploreFeatureR.string.explore_error_network)
                            is ExploreError.Unknown -> error.message ?: stringResource(ExploreFeatureR.string.explore_error_unknown)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    if (error !is ExploreError.ApiKeyMissing) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                        NeoBrutalButton(
                            text = stringResource(ExploreFeatureR.string.explore_error_retry),
                            onClick = onRetry,
                            backgroundColor = MaterialTheme.colorScheme.error
                        )
                    }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingLarge)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.wrapContentSize(),
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (currentLoadingMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))
                        AnimatedContent(
                            targetState = currentLoadingMessage,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(durationMillis = AnimationConstants.ANIMATION_DURATION))
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(durationMillis = AnimationConstants.ANIMATION_DURATION))
                                    )
                            },
                            label = "loadingMessageTransition"
                        ) { message ->
                            Text(
                                text = message,
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
