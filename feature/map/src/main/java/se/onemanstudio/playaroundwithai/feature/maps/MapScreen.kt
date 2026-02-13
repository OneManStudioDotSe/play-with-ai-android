package se.onemanstudio.playaroundwithai.feature.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.feature.map.R
import se.onemanstudio.playaroundwithai.feature.maps.MapConstants.STOCKHOLM_LAT
import se.onemanstudio.playaroundwithai.feature.maps.MapConstants.STOCKHOLM_LNG
import se.onemanstudio.playaroundwithai.feature.maps.states.MapError
import se.onemanstudio.playaroundwithai.feature.maps.states.MapUiState
import se.onemanstudio.playaroundwithai.feature.maps.views.CustomMarkerIcon
import se.onemanstudio.playaroundwithai.feature.maps.views.FilterChip
import se.onemanstudio.playaroundwithai.feature.maps.views.MarkerInfoCard
import se.onemanstudio.playaroundwithai.feature.maps.views.PathModeBar
import se.onemanstudio.playaroundwithai.feature.maps.views.SideControls

private const val CAMERA_PADDING = 150

@SuppressLint("MissingPermission", "GoogleMapComposable")
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    val stockholm = LatLng(STOCKHOLM_LAT, STOCKHOLM_LNG)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(stockholm, MapConstants.MAX_ZOOM_LEVEL)
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasLocationPermission = isGranted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { userLocation = LatLng(it.latitude, it.longitude) }
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

    LaunchedEffect(uiState.optimalRoute) {
        if (uiState.optimalRoute.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            uiState.optimalRoute.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), CAMERA_PADDING),
                durationMs = MapConstants.MOVE_TO_POINT_DURATION
            )
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
                        R.raw.custom_map_style_dark
                    } else {
                        R.raw.custom_map_style_light
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
                            stringResource(id = R.string.marker_content_description, item.name),
                            isSelected
                        )
                    }
                }
            }
        }

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
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
                FilterChip(
                    text = stringResource(id = R.string.scooters_filter_chip_label),
                    selected = uiState.activeFilter.contains(VehicleType.SCOOTER)
                ) { viewModel.toggleFilter(VehicleType.SCOOTER) }
                FilterChip(
                    text = stringResource(id = R.string.bicycles_filter_chip_label),
                    selected = uiState.activeFilter.contains(VehicleType.BICYCLE)
                ) { viewModel.toggleFilter(VehicleType.BICYCLE) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(Dimensions.paddingLarge)
        ) {
            SideControls(
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
                onSetPathMode = { viewModel.setPathMode(!uiState.isPathMode) }
            )
        }

        AnimatedVisibility(
            visible = uiState.isPathMode,
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
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(Dimensions.paddingMedium)
        ) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(Dimensions.paddingLarge)
            ) {
                PathModeBar(
                    count = uiState.selectedLocations.size,
                    distance = uiState.routeDistanceMeters,
                    duration = uiState.routeDurationMinutes,
                    onGoClick = {
                        if (userLocation != null) {
                            viewModel.calculateOptimalRoute(userLocation)
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.focusedMarker != null && !uiState.isPathMode,
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
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(Dimensions.paddingLarge)
            ) {
                uiState.focusedMarker?.let { marker ->
                    MarkerInfoCard(
                        marker = marker,
                        onClose = { viewModel.selectMarker(null) }
                    )
                }
            }
        }

        LoadingState(uiState)

        ErrorState(
            uiState = uiState,
            onRetry = { viewModel.loadMapData() }
        )
    }
}

@Composable
private fun ErrorState(
    uiState: MapUiState,
    onRetry: () -> Unit
) {
    uiState.error?.let { error ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
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
                    contentDescription = stringResource(R.string.map_error_icon),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = when (error) {
                        is MapError.NetworkError -> stringResource(R.string.map_error_network)
                        is MapError.Unknown -> error.message ?: stringResource(R.string.map_error_unknown)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                NeoBrutalButton(
                    text = stringResource(R.string.map_error_retry),
                    onClick = onRetry
                )
            }
        }
    }
}

@Composable
private fun LoadingState(uiState: MapUiState) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.wrapContentSize(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
