package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.explore.AnimationConstants
import se.onemanstudio.playaroundwithai.feature.explore.R

@Composable
internal fun BoxScope.PathModeHint(isVisible: Boolean) {
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
                text = stringResource(id = R.string.path_mode_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(Dimensions.paddingLarge)
            )
        }
    }
}

@Preview
@Composable
private fun PathModeHintPreview() {
    SofaAiTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                PathModeHint(isVisible = true)
            }
        }
    }
}
