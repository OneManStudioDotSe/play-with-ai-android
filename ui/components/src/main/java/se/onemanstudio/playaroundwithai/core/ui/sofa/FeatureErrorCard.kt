package se.onemanstudio.playaroundwithai.core.ui.sofa

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

/**
 * A NeoBrutal-styled error card used by feature screens.
 * Displays an icon, a title, a message, and an optional dismiss button.
 *
 * @param icon         The icon that represents the error type.
 * @param title        Short headline, e.g. "Oops!".
 * @param message      Full error description.
 * @param isDismissible When true, shows a dismiss button in the top-end corner.
 * @param dismissContentDescription Content description for the dismiss button.
 * @param onDismiss    Called when the user taps the dismiss button.
 */
@Composable
fun FeatureErrorCard(
    icon: ImageVector,
    title: String,
    message: String,
    isDismissible: Boolean,
    dismissContentDescription: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NeoBrutalCard(
        modifier = modifier
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimensions.iconSizeXLarge),
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }

            if (isDismissible) {
                NeoBrutalIconButton(
                    onClick = onDismiss,
                    imageVector = Icons.Default.Clear,
                    contentDescription = dismissContentDescription,
                    backgroundColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimensions.paddingMedium),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureErrorCardPreview() {
    SofaAiTheme {
        FeatureErrorCard(
            icon = Icons.Default.Clear,
            title = "Something went wrong",
            message = "We couldn't load the content. Please try again later.",
            isDismissible = true,
            dismissContentDescription = "Dismiss",
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeatureErrorCardNonDismissiblePreview() {
    SofaAiTheme {
        Surface {
            FeatureErrorCard(
                icon = Icons.Default.Clear,
                title = "Connection Error",
                message = "Check your internet connection and try again.",
                isDismissible = false,
                dismissContentDescription = "",
                onDismiss = {},
            )
        }
    }
}
