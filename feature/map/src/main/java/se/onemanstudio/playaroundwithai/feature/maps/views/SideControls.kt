package se.onemanstudio.playaroundwithai.feature.maps.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.map.R
import se.onemanstudio.playaroundwithai.feature.maps.MapConstants
import se.onemanstudio.playaroundwithai.feature.maps.state.MapUiState

@SuppressLint("MissingPermission")
@Composable
fun SideControls(
    uiState: MapUiState,
    cameraPositionState: CameraPositionState,
    fusedLocationClient: FusedLocationProviderClient,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    onSetPathMode: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    SideControlsContent(
        isPathMode = uiState.isPathMode,
        onZoomIn = {
            scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 500) }
        },
        onZoomOut = {
            scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 500) }
        },
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
        onPathModeClick = { onSetPathMode(uiState.isPathMode) }
    )
}

/**
 * Stateless UI: Pure Composable, easy to preview.
 */
@Composable
private fun SideControlsContent(
    isPathMode: Boolean,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onMyLocationClick: () -> Unit,
    onPathModeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(end = Dimensions.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        // zoom in
        NeoBrutalIconButton(
            onClick = onZoomIn,
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.zoom_in_button_content_description)
        )

        // zoom out
        NeoBrutalIconButton(
            onClick = onZoomOut,
            imageVector = Icons.Default.Remove,
            contentDescription = stringResource(id = R.string.zoom_out_button_content_description)
        )

        // my location
        NeoBrutalIconButton(
            onClick = onMyLocationClick,
            imageVector = Icons.Default.MyLocation,
            contentDescription = stringResource(id = R.string.my_location_button_content_description),
            backgroundColor = MaterialTheme.colorScheme.secondary
        )

        // path mode
        NeoBrutalIconButton(
            contentDescription = if (isPathMode) stringResource(id = R.string.exit_path_mode_button_content_description) else stringResource(
                id = R.string.enter_path_mode_button_content_description
            ),
            imageVector = if (isPathMode) Icons.Default.Close else Icons.Default.LinearScale,
            backgroundColor = if (isPathMode) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            shadowColor = if (isPathMode) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            onClick = onPathModeClick,
        )
    }
}

// --- PREVIEWS ---

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun SideControlsPreview_Light() {
    SofaAiTheme(darkTheme = false) {
        SideControlsContent(
            isPathMode = false,
            onZoomIn = {},
            onZoomOut = {},
            onMyLocationClick = {},
            onPathModeClick = {}
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SideControlsPreview_Dark() {
    SofaAiTheme(darkTheme = true) {
        SideControlsContent(
            isPathMode = false,
            onZoomIn = {},
            onZoomOut = {},
            onMyLocationClick = {},
            onPathModeClick = {}
        )
    }
}

@Preview(name = "Path Mode Active", showBackground = true)
@Composable
private fun SideControlsPreview_PathMode() {
    SofaAiTheme(darkTheme = false) {
        SideControlsContent(
            isPathMode = true, // Simulates the Red 'Close' button state
            onZoomIn = {},
            onZoomOut = {},
            onMyLocationClick = {},
            onPathModeClick = {}
        )
    }
}
