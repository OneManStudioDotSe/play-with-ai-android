package se.onemanstudio.playaroundwithai.feature.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.PrimaryBlack
import se.onemanstudio.playaroundwithai.core.ui.theme.PrimaryBlack_50
import se.onemanstudio.playaroundwithai.feature.map.R
import se.onemanstudio.playaroundwithai.feature.maps.MapConstants.STOCKHOLM_LAT
import se.onemanstudio.playaroundwithai.feature.maps.MapConstants.STOCKHOLM_LNG
import se.onemanstudio.playaroundwithai.feature.maps.models.VehicleType
import se.onemanstudio.playaroundwithai.feature.maps.views.CustomMarkerIcon
import se.onemanstudio.playaroundwithai.feature.maps.views.FilterChip
import se.onemanstudio.playaroundwithai.feature.maps.views.MarkerInfoCard
import se.onemanstudio.playaroundwithai.feature.maps.views.PathModeBar
import se.onemanstudio.playaroundwithai.feature.maps.views.SelfDismissingNotification

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
    var showLocationError by remember { mutableStateOf(false) }

    val focusedMarker = uiState.focusedMarker
    var markerToDisplay by remember { mutableStateOf(focusedMarker) }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasLocationPermission = isGranted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Continuously check permission status and fetch location if available
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { userLocation = LatLng(it.latitude, it.longitude) }
            }
        }
    }

    LaunchedEffect(uiState.optimalRoute) {
        if (uiState.optimalRoute.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            uiState.optimalRoute.forEach { boundsBuilder.include(it) }
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200),
                durationMs = MapConstants.MOVE_TO_POINT_DURATION
            )
        }
    }

    if (focusedMarker != null) {
        markerToDisplay = focusedMarker
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.custom_map_style),
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
            onMapClick = { viewModel.selectMarker(null) }
        ) {
            if (uiState.optimalRoute.isNotEmpty()) {
                Polyline(
                    points = uiState.optimalRoute,
                    color = PrimaryBlack,
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
                        keys = arrayOf(item.id, isSelected),
                        state = rememberMarkerState(position = item.position),
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
                        CustomMarkerIcon(icon, isSelected)
                    }
                }
            }
        }

        // the notification at the top if we don't have the user's current location
        AnimatedVisibility(
            visible = showLocationError,
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
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = Dimensions.paddingLarge)
        ) {
            SelfDismissingNotification(
                message = "Current location unknown. Cannot calculate path.",
                onDismiss = { showLocationError = false }
            )
        }

        // the filters for scooters and bicycles at the top
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
                FilterChip("Scooters", uiState.activeFilter.contains(VehicleType.SCOOTER)) { viewModel.toggleFilter(VehicleType.SCOOTER) }
                FilterChip("Bicycles", uiState.activeFilter.contains(VehicleType.BICYCLE)) { viewModel.toggleFilter(VehicleType.BICYCLE) }
            }
        }

        // Controls on the side
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = Dimensions.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
        ) {
            // zoom in
            NeoBrutalIconButton(
                onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 500) } },
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom In"
            )

            // zoom out
            NeoBrutalIconButton(
                onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 500) } },
                imageVector = Icons.Default.Remove,
                contentDescription = "Zoom Out"
            )

            // my location
            NeoBrutalIconButton(
                onClick = {
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
                        // If we don't have it, ask for it
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My location",
                backgroundColor = MaterialTheme.colorScheme.secondary
            )

            // path mode
            NeoBrutalIconButton(
                contentDescription = if (uiState.isPathMode) "Exit path mode" else "Enter path mode",
                imageVector = if (uiState.isPathMode) Icons.Default.Close else Icons.Default.LinearScale,
                backgroundColor = if (uiState.isPathMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                shadowColor = if (uiState.isPathMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                onClick = { viewModel.setPathMode(!uiState.isPathMode) },
            )
        }

        // the panel where we show info about the calculated path
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
                        } else {
                            showLocationError = true
                        }
                    }
                )
            }
        }

        // the panel with the marker's info
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
                markerToDisplay?.let { marker ->
                    MarkerInfoCard(
                        marker = marker,
                        onClose = { viewModel.selectMarker(null) }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = PrimaryBlack_50),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.wrapContentSize(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
