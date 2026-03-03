package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import se.onemanstudio.playaroundwithai.data.explore.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.feature.explore.AnimationConstants

@Composable
internal fun BoxScope.SuggestedPlaceInfoPanel(
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
                .heightIn(max = 400.dp)
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

@Preview
@Composable
private fun SuggestedPlaceInfoPanelPreview() {
    SofaAiTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                SuggestedPlaceInfoPanel(
                    place = SuggestedPlace(
                        name = "Royal Palace",
                        lat = 59.3268,
                        lng = 18.0717,
                        description = "The official residence of the Swedish monarch. A baroque-style palace with over 600 rooms.",
                        category = "Landmark"
                    ),
                    isPathMode = false,
                    onClose = {},
                )
            }
        }
    }
}
