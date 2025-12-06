package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun NeoBrutalIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    size: Dp = Dimensions.iconSizeLarge,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shadowColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowOffset = Dimensions.paddingSmall
    val pressOffset = Dimensions.paddingExtraSmall

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                translationX = if (isPressed) pressOffset.toPx() else 0f
                translationY = if (isPressed) pressOffset.toPx() else 0f
            }
            .neoBrutalism( // Using our custom modifier
                backgroundColor = backgroundColor,
                borderColor = shadowColor,
                borderWidth = Dimensions.neoBrutalCardStrokeWidth,
                shadowOffset = shadowOffset
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No ripple
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = shadowColor,
            modifier = Modifier.padding(Dimensions.paddingMedium)
        )
    }
}

@Composable
fun NeoBrutalButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    shadowColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowOffset = Dimensions.paddingMedium
    val pressOffset = Dimensions.paddingSmall

    Box(
        modifier = modifier
            .graphicsLayer {
                // Animate the press effect
                translationX = if (isPressed) pressOffset.toPx() else 0f
                translationY = if (isPressed) pressOffset.toPx() else 0f
            }
            .neoBrutalism(
                backgroundColor = backgroundColor,
                borderColor = shadowColor,
                shadowOffset = shadowOffset
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No ripple effect
                onClick = onClick
            )
            .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingLarge),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@Preview(name = "Buttons - Light Theme", showBackground = true, backgroundColor = 0xFFF8F8F8)
@Composable
private fun NeoBrutalButtonPreview_Light() {
    SofaAiTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)
        ) {
            NeoBrutalButton(
                onClick = {},
                text = "PRIMARY ACTION",
                backgroundColor = MaterialTheme.colorScheme.primary
            )
            NeoBrutalButton(
                onClick = {},
                text = "SECONDARY ACTION",
                backgroundColor = MaterialTheme.colorScheme.secondary
            )
            NeoBrutalIconButton(
                onClick = {},
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite Icon",
                backgroundColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Preview(name = "Buttons - Dark Theme", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun NeoBrutalButtonPreview_Dark() {
    SofaAiTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)
        ) {
            NeoBrutalButton(
                onClick = {},
                text = "PRIMARY ACTION",
                backgroundColor = MaterialTheme.colorScheme.primary
            )
            NeoBrutalButton(
                onClick = {},
                text = "SECONDARY ACTION",
                backgroundColor = MaterialTheme.colorScheme.secondary
            )
            NeoBrutalIconButton(
                onClick = {},
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite Icon",
                backgroundColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
