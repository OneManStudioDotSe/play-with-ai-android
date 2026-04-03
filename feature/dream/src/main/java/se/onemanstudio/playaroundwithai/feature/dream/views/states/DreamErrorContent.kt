package se.onemanstudio.playaroundwithai.feature.dream.views.states

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.FeatureErrorCard
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.dream.R
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamError
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamUiState

@Composable
fun DreamErrorContent(
    state: DreamUiState.Error,
    onClearError: () -> Unit,
) {
    val (errorMsg, errorIcon) = getErrorMessageAndIcon(state.error)

    FeatureErrorCard(
        icon = errorIcon,
        title = stringResource(R.string.dream_oops),
        message = errorMsg,
        isDismissible = state.error !is DreamError.ApiKeyMissing,
        dismissContentDescription = stringResource(R.string.dream_label_dismiss_error),
        onDismiss = onClearError,
    )
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
