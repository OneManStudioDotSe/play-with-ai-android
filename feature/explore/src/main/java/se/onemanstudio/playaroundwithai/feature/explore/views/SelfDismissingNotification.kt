package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import se.onemanstudio.playaroundwithai.core.ui.sofa.neoBrutalism
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.explore.R

@Composable
fun SelfDismissingNotification(
    message: String,
    onDismiss: () -> Unit
) {
    val totalTime = 3_000
    var timeLeft by remember { mutableIntStateOf(totalTime) }

    // Animate progress bar from 1.0 to 0.0
    val progress by animateFloatAsState(
        targetValue = timeLeft / totalTime.toFloat(),
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "Progress"
    )

    LaunchedEffect(Unit) {
        val step = 100L
        while (timeLeft > 0) {
            delay(step)
            timeLeft -= step.toInt()
        }
        onDismiss()
    }

    Box(
        modifier = Modifier
            .padding(horizontal = Dimensions.paddingLarge)
            .neoBrutalism(
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                borderColor = MaterialTheme.colorScheme.error,
                shadowOffset = Dimensions.neoBrutalCardShadowOffset
            )
            .padding(Dimensions.paddingMedium)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationDisabled,
                    contentDescription = stringResource(id = R.string.location_disabled_icon_content_description),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.heightMini),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = Alphas.extraLow),
            )
        }
    }
}

@Preview(name = "Default")
@Composable
private fun SelfDismissingNotificationDefaultPreview() {
    SofaAiTheme {
        SelfDismissingNotification(
            message = "Location services are disabled.",
            onDismiss = {}
        )
    }
}

@Preview(name = "Long Message")
@Composable
private fun SelfDismissingNotificationLongMessagePreview() {
    SofaAiTheme {
        SelfDismissingNotification(
            message = "This is a much longer notification message to see how the layout handles more text.",
            onDismiss = {}
        )
    }
}
