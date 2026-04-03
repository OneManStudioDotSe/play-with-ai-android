package se.onemanstudio.playaroundwithai.feature.plan.views.states

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
import se.onemanstudio.playaroundwithai.feature.plan.R
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanError
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState

@Composable
internal fun ErrorState(
    state: PlanUiState.Error,
    onClearError: () -> Unit,
) {
    val (errorMsg, errorIcon) = getErrorMessageAndIcon(state.error)

    FeatureErrorCard(
        icon = errorIcon,
        title = stringResource(R.string.plan_oops),
        message = errorMsg,
        isDismissible = state.error !is PlanError.ApiKeyMissing,
        dismissContentDescription = stringResource(R.string.plan_label_dismiss_error),
        onDismiss = onClearError,
    )
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
