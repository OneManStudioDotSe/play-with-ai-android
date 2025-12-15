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
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = Dimensions.minButtonHeight)
            .wrapContentSize()
            .clickable { onClick() }
            .neoBrutalism(
                backgroundColor = backgroundColor,
                shadowOffset = Dimensions.neoBrutalCardShadowOffset
            )
            .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingMedium)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun FilterChipPreview() {
    SofaAiTheme {
        FilterChip(
            text = "Scooters",
            selected = false,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FilterChipPreviewSelected() {
    SofaAiTheme {
        FilterChip(
            text = "Bicycles",
            selected = true,
            onClick = {}
        )
    }
}
