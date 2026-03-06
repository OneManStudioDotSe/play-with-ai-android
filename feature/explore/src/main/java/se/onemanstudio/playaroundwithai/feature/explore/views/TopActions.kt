package se.onemanstudio.playaroundwithai.feature.explore.views

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.feature.explore.AnimationConstants
import se.onemanstudio.playaroundwithai.feature.explore.R
import se.onemanstudio.playaroundwithai.feature.explore.states.ExploreUiState
import se.onemanstudio.playaroundwithai.feature.explore.states.MarkersState

@Composable
internal fun BoxScope.TopActions(
    uiState: ExploreUiState,
    onToggleFilter: (VehicleType) -> Unit,
    onSuggestPlaces: () -> Unit,
) {
    val view = LocalView.current

    AnimatedVisibility(
        visible = !uiState.pathMode.isActive,
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
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .padding(top = Dimensions.paddingMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.paddingLarge),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                text = stringResource(id = R.string.scooters_filter_chip_label),
                selected = uiState.markers.activeFilter.contains(VehicleType.Scooter)
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onToggleFilter(VehicleType.Scooter)
            }

            //Spacer(modifier = Modifier.width(Dimensions.paddingSmall))

            FilterChip(
                text = stringResource(id = R.string.bicycles_filter_chip_label),
                selected = uiState.markers.activeFilter.contains(VehicleType.Bicycle)
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                onToggleFilter(VehicleType.Bicycle)
            }

            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))

            @OptIn(ExperimentalMaterial3ExpressiveApi::class)
            AnimatedVisibility(visible = uiState.markers.isLoading) {
                LoadingIndicator(
                    modifier = Modifier
                        .padding(end = Dimensions.paddingSmall)
                        .size(Dimensions.iconSizeLarge),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            NeoBrutalIconButton(
                modifier = Modifier.padding(end = Dimensions.paddingSmall),
                backgroundColor = energeticOrange,
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = stringResource(id = R.string.ai_suggest_button_content_description),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onSuggestPlaces()
                },
            )
        }
    }
}

@Preview
@Composable
private fun TopActionsPreview() {
    SofaAiTheme {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                TopActions(
                    uiState = ExploreUiState(markers = MarkersState(isLoading = true)),
                    onToggleFilter = {},
                    onSuggestPlaces = {},
                )
            }
        }
    }
}
