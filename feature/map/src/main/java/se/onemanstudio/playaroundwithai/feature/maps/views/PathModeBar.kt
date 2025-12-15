package se.onemanstudio.playaroundwithai.feature.maps.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun PathModeBar(
    count: Int,
    distance: Int,
    duration: Int,
    onGoClick: () -> Unit
) {
    val isRouteCalculated = distance > 0

    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(Dimensions.paddingLarge)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                if (isRouteCalculated) {
                    Text(text = "TOTAL DISTANCE", style = MaterialTheme.typography.labelSmall)
                    Text(text = "$distance m • $duration min", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(
                        text = "PATH MODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$count Points Selected",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            NeoBrutalButton(
                text = if (isRouteCalculated) "RECALCULATE" else "GO",
                onClick = onGoClick,
                enabled = count > 1,
                icon = if (isRouteCalculated) Icons.Default.Refresh else Icons.Default.PlayArrow,
                backgroundColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
fun PathModeBarPreview_Disabled() {
    SofaAiTheme {
        PathModeBar(
            count = 1,
            distance = 42,
            duration = 124,
            onGoClick = {}
        )
    }
}

@Preview
@Composable
fun PathModeBarPreview_Enabled() {
    SofaAiTheme {
        PathModeBar(
            count = 2,
            distance = 42,
            duration = 124,
            onGoClick = {}
        )
    }
}
