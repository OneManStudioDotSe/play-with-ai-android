package se.onemanstudio.playaroundwithai.feature.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanError
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState

@Composable
internal fun ErrorState(
    state: PlanUiState.Error,
    onClearError: () -> Unit,
) {
    val (errorMsg, errorIcon) = getErrorMessageAndIcon(state.error)

    NeoBrutalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.paddingLarge),
    ) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingLarge),
            ) {
                Icon(
                    imageVector = errorIcon,
                    contentDescription = stringResource(R.string.plan_label_error_icon),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimensions.iconSizeXLarge),
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = stringResource(R.string.plan_oops),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }

            if (state.error !is PlanError.ApiKeyMissing) {
                NeoBrutalIconButton(
                    onClick = onClearError,
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.plan_label_dismiss_error),
                    backgroundColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimensions.paddingMedium),
                )
            }
        }
    }
}

@Composable
private fun getErrorMessageAndIcon(error: PlanError): Pair<String, ImageVector> {
    return when (error) {
        is PlanError.ApiKeyMissing -> stringResource(R.string.plan_error_api_key_missing) to Icons.Rounded.VpnKey
        is PlanError.NetworkMissing -> stringResource(R.string.plan_error_network) to Icons.Rounded.WifiOff
        is PlanError.Unknown -> (error.message ?: stringResource(R.string.plan_error_unknown)) to Icons.Rounded.Warning
    }
}

@Preview(name = "Error Light")
@Composable
private fun ErrorStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            ErrorState(
                state = PlanUiState.Error(error = PlanError.NetworkMissing),
                onClearError = {},
            )
        }
    }
}

@Preview(name = "Error Dark")
@Composable
private fun ErrorStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            ErrorState(
                state = PlanUiState.Error(error = PlanError.ApiKeyMissing),
                onClearError = {},
            )
        }
    }
}
