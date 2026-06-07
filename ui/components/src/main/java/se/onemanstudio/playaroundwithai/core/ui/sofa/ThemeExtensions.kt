package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions

fun Modifier.neoBrutalism(
    backgroundColor: Color,
    borderColor: Color,
    borderWidth: Dp = Dimensions.neoBrutalCardStrokeWidth,
    shadowOffset: Dp = Dimensions.neoBrutalCardShadowOffset
): Modifier = this.then(
    Modifier.drawBehind {
        drawIntoCanvas {
            // Draw the shadow
            drawRect(
                color = borderColor,
                topLeft = Offset(shadowOffset.toPx(), shadowOffset.toPx()),
                size = this.size
            )
            // Draw the background
            drawRect(
                color = backgroundColor,
                size = this.size
            )
            // Draw the border
            drawRect(
                color = borderColor,
                size = this.size,
                style = Stroke(width = borderWidth.toPx())
            )
        }
    }
)
