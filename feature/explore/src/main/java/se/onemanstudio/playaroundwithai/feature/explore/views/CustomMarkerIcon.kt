package se.onemanstudio.playaroundwithai.feature.explore.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun CustomMarkerIcon(
    icon: ImageVector,
    iconContentDescription: String?,
    isSelected: Boolean
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.onSurface
    val iconColor = contentColorFor(backgroundColor)

    Box(
        modifier = Modifier
            .size(if (isSelected) Dimensions.iconSizeXXLarge else Dimensions.iconSizeXLarge)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(Dimensions.neoBrutalCardStrokeWidth, borderColor, CircleShape)
            .padding(Dimensions.paddingMedium),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            tint = iconColor
        )
    }
}

@Preview(name = "Light - Unselected")
@Composable
private fun CustomMarkerIconUnselectedLightPreview() {
    SofaAiTheme(darkTheme = false) {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            iconContentDescription = "Home icon",
            isSelected = false
        )
    }
}

@Preview(name = "Light - Selected")
@Composable
private fun CustomMarkerIconSelectedLightPreview() {
    SofaAiTheme(darkTheme = false) {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            iconContentDescription = "Home icon",
            isSelected = true
        )
    }
}

@Preview(name = "Dark - Unselected")
@Composable
private fun CustomMarkerIconUnselectedDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            iconContentDescription = "Home icon",
            isSelected = false
        )
    }
}

@Preview(name = "Dark - Selected")
@Composable
private fun CustomMarkerIconSelectedDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            iconContentDescription = "Home icon",
            isSelected = true
        )
    }
}
