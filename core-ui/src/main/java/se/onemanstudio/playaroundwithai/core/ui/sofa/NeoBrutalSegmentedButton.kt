package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.data.InputMode
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun NeoBrutalSegmentedButton(
    modes: List<InputMode>,
    selectedMode: InputMode,
    onModeSelected: (InputMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .neoBrutalism(
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.onSurface,
                shadowOffset = 4.dp
            )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            modes.forEachIndexed { index, mode ->
                val isSelected = mode == selectedMode

                // Animate background color change
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(200),
                    label = "segment_bg"
                )

                // Animate text color change
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(200),
                    label = "segment_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(backgroundColor)
                        .clickable { onModeSelected(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = textColor
                    )
                }

                // Divider between items
                if (index < modes.lastIndex) {
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        thickness = Dimensions.borderStrokeSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SegmentedButtonPreview() {
    SofaAiTheme {
        NeoBrutalSegmentedButton(
            modes = InputMode.values().toList(),
            selectedMode = InputMode.TEXT,
            onModeSelected = {}
        )
    }
}
