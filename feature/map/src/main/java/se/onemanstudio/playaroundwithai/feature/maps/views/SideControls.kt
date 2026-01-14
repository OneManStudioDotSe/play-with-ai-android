package se.onemanstudio.playaroundwithai.feature.maps.views

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
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.feature.map.R
import se.onemanstudio.playaroundwithai.feature.maps.states.MapUiState

@Composable
fun SideControls(
    uiState: MapUiState,
    cameraPositionState: CameraPositionState,
    onMyLocationClick: () -> Unit,
    onSetPathMode: (Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(end = Dimensions.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        // zoom in
        NeoBrutalIconButton(
            onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 500) } },
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(id = R.string.zoom_in_button_content_description)
        )

        // zoom out
        NeoBrutalIconButton(
            onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 500) } },
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
            contentDescription = if (uiState.isPathMode) stringResource(id = R.string.exit_path_mode_button_content_description) else stringResource(
                id = R.string.enter_path_mode_button_content_description
            ),
            imageVector = if (uiState.isPathMode) Icons.Default.Close else Icons.Default.LinearScale,
            backgroundColor = if (uiState.isPathMode) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            shadowColor = if (uiState.isPathMode) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            onClick = { onSetPathMode(uiState.isPathMode) },
        )
    }
}
