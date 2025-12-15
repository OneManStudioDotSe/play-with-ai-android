package se.onemanstudio.playaroundwithai.feature.maps.views

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
    // 1. Determine Background Color based on selection
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface

    // 2. Determine Border Color (typically 'onSurface' handles Black/White switch)
    val borderColor = MaterialTheme.colorScheme.onSurface

    // 3. Determine Icon Color (ensures contrast against the background)
    val iconColor = contentColorFor(backgroundColor)

    Box(
        modifier = Modifier
            .size(if (isSelected) Dimensions.iconSizeXXLarge else Dimensions.iconSizeXLarge)
            .clip(CircleShape)
            .background(backgroundColor)
            // Use dynamic borderColor instead of Color.Black
            .border(Dimensions.neoBrutalCardStrokeWidth, borderColor, CircleShape)
            .padding(Dimensions.paddingMedium),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            // Use dynamic iconColor instead of Color.Black
            tint = iconColor
        )
    }
}

// --- Previews ---

@Preview(name = "Light Mode - Unselected", showBackground = true)
@Composable
fun CustomMarkerIconPreview_Light() {
    SofaAiTheme(darkTheme = false) {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            iconContentDescription = "Home icon",
            isSelected = false
        )
    }
}

@Preview(name = "Light Mode - Selected", showBackground = true)
@Composable
fun CustomMarkerIconPreview_Selected_Light() {
    SofaAiTheme(darkTheme = false) {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            iconContentDescription = "Home icon",
            isSelected = true
        )
    }
}

@Preview(name = "Dark Mode - Unselected", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CustomMarkerIconPreview_Dark() {
    SofaAiTheme(darkTheme = true) {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            iconContentDescription = "Home icon",
            isSelected = false
        )
    }
}

@Preview(name = "Dark Mode - Selected", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CustomMarkerIconPreview_Selected_Dark() {
    SofaAiTheme(darkTheme = true) {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            iconContentDescription = "Home icon",
            isSelected = true
        )
    }
}
