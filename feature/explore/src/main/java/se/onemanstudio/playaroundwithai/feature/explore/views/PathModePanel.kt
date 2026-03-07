package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.feature.explore.AnimationConstants

@Composable
internal fun BoxScope.PathModePanel(
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
