package se.onemanstudio.playaroundwithai.feature.dream.views

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
import se.onemanstudio.playaroundwithai.feature.dream.R
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamError
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamUiState

@Composable
internal fun DreamErrorContent(
    state: DreamUiState.Error,
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
                    contentDescription = stringResource(R.string.dream_label_error_icon),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimensions.iconSizeXLarge),
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = stringResource(R.string.dream_oops),
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

            if (state.error !is DreamError.ApiKeyMissing) {
                NeoBrutalIconButton(
                    onClick = onClearError,
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.dream_label_dismiss_error),
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
private fun getErrorMessageAndIcon(error: DreamError): Pair<String, ImageVector> {
    return when (error) {
        is DreamError.ApiKeyMissing -> stringResource(R.string.dream_error_api_key_missing) to Icons.Rounded.VpnKey
        is DreamError.NetworkMissing -> stringResource(R.string.dream_error_network) to Icons.Rounded.WifiOff
        is DreamError.InputTooLong -> stringResource(R.string.dream_error_input_too_long) to Icons.Rounded.Warning
        is DreamError.Unknown -> (error.message ?: stringResource(R.string.dream_error_unknown)) to Icons.Rounded.Warning
    }
}

@Preview(name = "Error Light")
@Composable
private fun DreamErrorContentLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            DreamErrorContent(
                state = DreamUiState.Error(error = DreamError.NetworkMissing),
                onClearError = {},
            )
        }
    }
}

@Preview(name = "Error Dark")
@Composable
private fun DreamErrorContentDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            DreamErrorContent(
                state = DreamUiState.Error(error = DreamError.ApiKeyMissing),
                onClearError = {},
            )
        }
    }
}
