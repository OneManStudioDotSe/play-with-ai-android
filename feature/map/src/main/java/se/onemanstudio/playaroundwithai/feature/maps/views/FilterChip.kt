package se.onemanstudio.playaroundwithai.feature.maps.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.neoBrutalism
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // 1. Determine colors based on state & theme
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    // 2. Dynamic border/shadow color (Black in Light Mode, White in Dark Mode)
    val outlineColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = Dimensions.minButtonHeight)
            .wrapContentSize()
            .neoBrutalism(
                backgroundColor = backgroundColor,
                borderColor = outlineColor,
                shadowOffset = Dimensions.neoBrutalCardShadowOffset
            )
            .clickable { onClick() }
            .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingMedium)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}

@Preview(name = "Light - Unselected")
@Composable
private fun FilterChipUnselectedLightPreview() {
    SofaAiTheme(darkTheme = false) {
        FilterChip(
            text = "Scooters",
            selected = false,
            onClick = {}
        )
    }
}

@Preview(name = "Light - Selected")
@Composable
private fun FilterChipSelectedLightPreview() {
    SofaAiTheme(darkTheme = false) {
        FilterChip(
            text = "Bicycles",
            selected = true,
            onClick = {}
        )
    }
}

@Preview(name = "Dark - Unselected")
@Composable
private fun FilterChipUnselectedDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        FilterChip(
            text = "Scooters",
            selected = false,
            onClick = {}
        )
    }
}

@Preview(name = "Dark - Selected")
@Composable
private fun FilterChipSelectedDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        FilterChip(
            text = "Bicycles",
            selected = true,
            onClick = {}
        )
    }
}
