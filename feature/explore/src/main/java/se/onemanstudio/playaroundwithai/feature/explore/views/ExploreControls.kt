package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.explore.states.ExploreUiState

@Composable
internal fun BoxScope.ExploreControls(
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

@Preview(name = "Path Mode Off")
@Composable
private fun ExploreControlsPathModeOffPreview() {
    SofaAiTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                ExploreControls(
                    uiState = ExploreUiState(isPathMode = false),
                    cameraPositionState = rememberCameraPositionState(),
                    onMyLocationClick = {},
                    onSetPathMode = {}
                )
            }
        }
    }
}

@Preview(name = "Path Mode On")
@Composable
private fun ExploreControlsPathModeOnPreview() {
    SofaAiTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                ExploreControls(
                    uiState = ExploreUiState(isPathMode = true),
                    cameraPositionState = rememberCameraPositionState(),
                    onMyLocationClick = {},
                    onSetPathMode = {}
                )
            }
        }
    }
}
