package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.feature.explore.R

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
                    Text(text = stringResource(R.string.total_distance), style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = stringResource(R.string.distance_duration, distance, duration),
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        text = stringResource(R.string.path_mode_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.points_selected, count),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimensions.paddingLarge))

            NeoBrutalButton(
                text = if (isRouteCalculated) stringResource(R.string.recalculate) else stringResource(R.string.go),
                onClick = onGoClick,
                enabled = count > 1,
                icon = if (isRouteCalculated) Icons.Default.Refresh else Icons.Default.PlayArrow,
                iconContentDescription = if (isRouteCalculated) {
                    stringResource(R.string.recalculate_icon_content_description)
                } else {
                    stringResource(R.string.go_icon_content_description)
                },
                backgroundColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(name = "Disabled")
@Composable
private fun PathModeBarDisabledPreview() {
    SofaAiTheme {
        PathModeBar(
            count = 1,
            distance = 42,
            duration = 124,
            onGoClick = {}
        )
    }
}

@Preview(name = "Enabled")
@Composable
private fun PathModeBarEnabledPreview() {
    SofaAiTheme {
        PathModeBar(
            count = 2,
            distance = 42,
            duration = 124,
            onGoClick = {}
        )
    }
}
