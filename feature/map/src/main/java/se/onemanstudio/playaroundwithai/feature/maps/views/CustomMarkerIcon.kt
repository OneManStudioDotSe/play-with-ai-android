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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun CustomMarkerIcon(
    icon: ImageVector,
    isSelected: Boolean
) {
    val color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .size(if (isSelected) Dimensions.iconSizeXXLarge else Dimensions.iconSizeXLarge)
            .clip(CircleShape)
            .background(color)
            .border(Dimensions.neoBrutalCardStrokeWidth, Color.Black, CircleShape)
            .padding(Dimensions.paddingMedium),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black
        )
    }
}

@Preview
@Composable
fun CustomMarkerIconPreview() {
    SofaAiTheme {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            isSelected = false
        )
    }
}

@Preview
@Composable
fun CustomMarkerIconPreview_Selected() {
    SofaAiTheme {
        CustomMarkerIcon(
            icon = Icons.Default.Home,
            isSelected = true
        )
    }
}
