package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.disabledBackground
import se.onemanstudio.playaroundwithai.core.ui.theme.disabledBorder
import se.onemanstudio.playaroundwithai.core.ui.theme.disabledContent
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme

@Composable
fun NeoBrutalButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    shadowColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowOffset = if (enabled) Dimensions.paddingSmall else Dimensions.none
    val pressOffset = if (enabled) Dimensions.paddingSmall else Dimensions.none

    val activeBackgroundColor = if (enabled) backgroundColor else disabledBackground
    val activeContentColor = if (enabled) MaterialTheme.colorScheme.onSecondary else disabledContent
    val activeBorderColor = if (enabled) shadowColor else disabledBorder

    Box(
        modifier = modifier
            .drawBehind {
                drawRect(
                    color = activeBorderColor,
                    topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                    size = this.size
                )
            }
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = if (isPressed) pressOffset.toPx() else 0f
                    translationY = if (isPressed) pressOffset.toPx() else 0f
                }
                .drawBehind {
                    drawRect(color = activeBackgroundColor, size = this.size)
                    drawRect(
                        color = activeBorderColor,
                        size = this.size,
                        style = Stroke(Dimensions.neoBrutalCardStrokeWidth.toPx())
                    )
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(horizontal = Dimensions.paddingLarge, vertical = Dimensions.paddingLarge),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconContentDescription,
                        tint = activeContentColor
                    )
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = activeContentColor
                )
            }
        }
    }
}

@Composable
fun NeoBrutalIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    size: Dp = Dimensions.iconSizeLarge,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    shadowColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shadowOffset = Dimensions.paddingSmall
    val pressOffset = Dimensions.paddingSmall

    Box(
        modifier = modifier
            .sizeIn(minWidth = Dimensions.minTouchTarget, minHeight = Dimensions.minTouchTarget)
            .size(size)
            .drawBehind {
                drawRect(
                    color = shadowColor,
                    topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                    size = this.size
                )
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = if (isPressed) pressOffset.toPx() else 0f
                    translationY = if (isPressed) pressOffset.toPx() else 0f
                }
                .drawBehind {
                    drawRect(color = backgroundColor, size = this.size)
                    drawRect(
                        color = shadowColor,
                        size = this.size,
                        style = Stroke(Dimensions.neoBrutalCardStrokeWidth.toPx())
                    )
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.padding(Dimensions.paddingMedium)
            )
        }
    }
}

@Preview(name = "Light")
@Composable
private fun NeoBrutalButtonLightPreview() {
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
            NeoBrutalButton(
                onClick = {},
                text = "DISABLED",
                enabled = false,
                icon = Icons.Default.Navigation,
                iconContentDescription = "Navigation icon"
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

@Preview(name = "Dark")
@Composable
private fun NeoBrutalButtonDarkPreview() {
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
