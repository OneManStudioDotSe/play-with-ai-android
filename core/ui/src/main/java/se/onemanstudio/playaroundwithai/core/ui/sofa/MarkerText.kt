package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime

private const val MARKER_HEIGHT_FRACTION = 0.6f
private const val MARKER_OVERSHOOT_DP = 12f
private const val MARKER_SKEW_DP = 3f
private const val MARKER_ALPHA = 0.35f

@Composable
fun MarkerText(
    text: String,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    var textWidth by remember { mutableFloatStateOf(0f) }

    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        onTextLayout = { result -> textWidth = result.size.width.toFloat() },
        modifier = modifier
            .drawBehind {
                if (textWidth <= 0f) return@drawBehind

                val markerHeight = size.height * MARKER_HEIGHT_FRACTION
                val overshoot = MARKER_OVERSHOOT_DP.dp.toPx()
                val markerWidth = textWidth + overshoot
                val verticalCenter = size.height / 2f
                val skew = MARKER_SKEW_DP.dp.toPx()

                val path = Path().apply {
                    moveTo(0f, verticalCenter - markerHeight / 2f + skew)
                    lineTo(markerWidth, verticalCenter - markerHeight / 2f)
                    lineTo(markerWidth, verticalCenter + markerHeight / 2f - skew)
                    lineTo(0f, verticalCenter + markerHeight / 2f)
                    close()
                }

                drawPath(
                    path = path,
                    color = lineColor.copy(alpha = MARKER_ALPHA),
                )
            },
    )
}

@Preview(name = "Marker Text Variants")
@Composable
private fun MarkerTextPreview() {
    SofaAiTheme {
        Column(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge),
        ) {
            MarkerText(text = "Map Controls", lineColor = electricBlue)
            MarkerText(text = "Weekly Usage", lineColor = vividPink)
            MarkerText(text = "About", lineColor = zestyLime)
        }
    }
}
