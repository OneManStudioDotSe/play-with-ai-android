package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.data.explore.domain.model.ExploreItem
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.feature.explore.AnimationConstants
import se.onemanstudio.playaroundwithai.feature.explore.models.ExploreItemUiModel

@Composable
internal fun BoxScope.MarkerInfoPanel(
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

@Preview
@Composable
private fun MarkerInfoPanelPreview() {
    SofaAiTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                MarkerInfoPanel(
                    marker = ExploreItemUiModel(
                        mapItem = ExploreItem(
                            id = "1",
                            lat = 59.3293,
                            lng = 18.0686,
                            name = "Cool E-Scooter",
                            type = VehicleType.Scooter,
                            batteryLevel = 78,
                            vehicleCode = "ABCD",
                            nickname = "Scooty McScootface"
                        ),
                        isSelected = false
                    ),
                    isPathMode = false,
                    onClose = {}
                )
            }
        }
    }
}
