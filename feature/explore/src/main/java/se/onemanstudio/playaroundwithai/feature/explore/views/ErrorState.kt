package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.explore.R
import se.onemanstudio.playaroundwithai.feature.explore.states.ExploreError

@Composable
fun ErrorState(
    error: ExploreError?,
    onRetry: () -> Unit
) {
    error?.let { error ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface.copy(alpha = Alphas.high)),
            contentAlignment = Alignment.Center
        ) {
            NeoBrutalCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(Dimensions.paddingLarge),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(Dimensions.paddingLarge)
                ) {
                    Icon(
                        imageVector = when (error) {
                            is ExploreError.ApiKeyMissing -> Icons.Rounded.VpnKey
                            is ExploreError.NetworkError -> Icons.Rounded.WifiOff
                            is ExploreError.Unknown -> Icons.Rounded.Warning
                        },
                        contentDescription = stringResource(R.string.explore_error_icon),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimensions.iconSizeXLarge)
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                    Text(
                        text = stringResource(R.string.explore_error_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                    Text(
                        text = when (error) {
                            is ExploreError.ApiKeyMissing -> stringResource(R.string.error_maps_api_key_missing)
                            is ExploreError.NetworkError -> stringResource(R.string.explore_error_network)
                            is ExploreError.Unknown -> error.message ?: stringResource(R.string.explore_error_unknown)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    if (error !is ExploreError.ApiKeyMissing) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                        NeoBrutalButton(
                            text = stringResource(R.string.explore_error_retry),
                            onClick = onRetry,
                            backgroundColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "API Key Missing")
@Composable
private fun ErrorStateApiKeyMissingPreview() {
    SofaAiTheme {
        Surface {
            ErrorState(
                error = ExploreError.ApiKeyMissing,
                onRetry = {}
            )
        }
    }
}

@Preview(name = "Network Error")
@Composable
private fun ErrorStateNetworkPreview() {
    SofaAiTheme {
        Surface {
            ErrorState(
                error = ExploreError.NetworkError,
                onRetry = {}
            )
        }
    }
}

@Preview(name = "Unknown Error")
@Composable
private fun ErrorStateUnknownPreview() {
    SofaAiTheme {
        Surface {
            ErrorState(
                error = ExploreError.Unknown("Something went terribly wrong."),
                onRetry = {}
            )
        }
    }
}
