package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.explore.AnimationConstants

@Composable
internal fun LoadingState(
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
                    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
                    LoadingIndicator(
                        modifier = Modifier.wrapContentSize(),
                        color = MaterialTheme.colorScheme.primary,
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

@Preview
@Composable
private fun LoadingStatePreview() {
    SofaAiTheme {
        Surface {
            LoadingState(
                isLoading = true,
                currentLoadingMessage = "Consulting the oracle..."
            )
        }
    }
}
