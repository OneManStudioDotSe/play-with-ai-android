@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.dream.views

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamElement
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamLayer
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamParticle
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ElementShape
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val ANIMATION_DURATION_MS = 20_000
private const val MOUNTAIN_PEAK_RATIO = 0.6f
private const val WAVE_CONTROL_OFFSET = 0.15f
private const val WAVE_SECOND_CONTROL = 1.5f
private const val WAVE_SECOND_END = 2f
private const val WAVE_STROKE_RATIO = 0.1f
private const val TREE_TRUNK_WIDTH_RATIO = 0.15f
private const val TREE_TRUNK_HEIGHT_RATIO = 0.4f
private const val TREE_TRUNK_ALPHA = 0.8f
private const val TREE_CANOPY_WIDTH_RATIO = 0.5f
private const val CLOUD_OVAL_RATIO = 0.35f
private const val CLOUD_SIDE_SCALE = 1.5f
private const val CLOUD_SIDE_OFFSET = 0.2f
private const val CLOUD_SIDE_HEIGHT = 0.8f
private const val STAR_INNER_RATIO = 0.4f
private const val STAR_POINTS = 5
private const val FULL_CIRCLE_DEGREES = 360.0
private const val STAR_ROTATION_OFFSET = 90.0
private const val HALF_ROTATION = 0.5f
private const val SPARKLE_LINE_RATIO = 0.7f
private const val RING_STROKE_RATIO = 0.3f
private const val TWO_PI = (2.0 * PI).toFloat()
private const val PARALLAX_WRAP = 1.5f
private const val PARALLAX_OFFSET = 0.25f
private const val ELEMENT_SIZE_RATIO = 0.1f
private const val PARTICLE_DRIFT_RATIO = 0.05f
private const val DEGREES_TO_RADIANS = 180.0

@Composable
fun DreamscapeCanvas(
    scene: DreamScene,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dreamscape")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = ANIMATION_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "time",
    )

    Canvas(modifier = modifier) {
        drawSkyGradient(scene.palette)

        scene.layers.sortedBy { it.depth }.forEach { layer ->
            drawDreamLayer(layer, time)
        }

        drawParticles(scene.particles, time)
    }
}

private fun DrawScope.drawSkyGradient(palette: DreamPalette) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(palette.sky), Color(palette.horizon)),
        ),
        size = size,
    )
}

private fun DrawScope.drawDreamLayer(layer: DreamLayer, time: Float) {
    val layerOffset = time * layer.depth * size.width
    layer.elements.forEach { element ->
        drawDreamElement(element, layerOffset)
    }
}

@Suppress("CyclomaticComplexMethod")
private fun DrawScope.drawDreamElement(element: DreamElement, layerOffset: Float) {
    val baseX = element.x * size.width
    val baseY = element.y * size.height
    val offsetX = (baseX + layerOffset) % (size.width * PARALLAX_WRAP) - size.width * PARALLAX_OFFSET
    val elementSize = element.scale * size.width * ELEMENT_SIZE_RATIO
    val color = Color(element.color).copy(alpha = element.alpha)

    when (element.shape) {
        ElementShape.CIRCLE -> drawCircle(color = color, radius = elementSize / 2f, center = Offset(offsetX, baseY))
        ElementShape.TRIANGLE -> drawTriangle(color, offsetX, baseY, elementSize)
        ElementShape.MOUNTAIN -> drawMountain(color, offsetX, baseY, elementSize)
        ElementShape.WAVE -> drawWave(color, offsetX, baseY, elementSize)
        ElementShape.TREE -> drawTree(color, offsetX, baseY, elementSize)
        ElementShape.CLOUD -> drawCloud(color, offsetX, baseY, elementSize)
        ElementShape.STAR -> drawStar(color, offsetX, baseY, elementSize)
    }
}

private fun DrawScope.drawTriangle(color: Color, x: Float, y: Float, elementSize: Float) {
    val path = Path().apply {
        moveTo(x, y - elementSize / 2f)
        lineTo(x - elementSize / 2f, y + elementSize / 2f)
        lineTo(x + elementSize / 2f, y + elementSize / 2f)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawMountain(color: Color, x: Float, y: Float, elementSize: Float) {
    val path = Path().apply {
        moveTo(x, y - elementSize * MOUNTAIN_PEAK_RATIO)
        lineTo(x - elementSize, y + elementSize / 2f)
        lineTo(x + elementSize, y + elementSize / 2f)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawWave(color: Color, x: Float, y: Float, elementSize: Float) {
    val path = Path().apply {
        moveTo(x - elementSize, y)
        cubicTo(
            x - elementSize / 2f, y - elementSize * WAVE_CONTROL_OFFSET,
            x, y + elementSize * WAVE_CONTROL_OFFSET,
            x + elementSize / 2f, y,
        )
        cubicTo(
            x + elementSize, y - elementSize * WAVE_CONTROL_OFFSET,
            x + elementSize * WAVE_SECOND_CONTROL, y + elementSize * WAVE_CONTROL_OFFSET,
            x + elementSize * WAVE_SECOND_END, y,
        )
    }
    drawPath(path, color, style = Stroke(width = elementSize * WAVE_STROKE_RATIO))
}

private fun DrawScope.drawTree(color: Color, x: Float, y: Float, elementSize: Float) {
    val trunkWidth = elementSize * TREE_TRUNK_WIDTH_RATIO
    val trunkHeight = elementSize * TREE_TRUNK_HEIGHT_RATIO
    drawRect(
        color = color.copy(alpha = color.alpha * TREE_TRUNK_ALPHA),
        topLeft = Offset(x - trunkWidth / 2f, y),
        size = Size(trunkWidth, trunkHeight),
    )

    val canopyPath = Path().apply {
        moveTo(x, y - elementSize * TREE_CANOPY_WIDTH_RATIO)
        lineTo(x - elementSize * TREE_CANOPY_WIDTH_RATIO, y)
        lineTo(x + elementSize * TREE_CANOPY_WIDTH_RATIO, y)
        close()
    }
    drawPath(canopyPath, color)
}

private fun DrawScope.drawCloud(color: Color, x: Float, y: Float, elementSize: Float) {
    val ovalSize = elementSize * CLOUD_OVAL_RATIO
    drawOval(color = color, topLeft = Offset(x - ovalSize, y - ovalSize / 2f), size = Size(ovalSize * 2f, ovalSize))
    drawOval(
        color = color,
        topLeft = Offset(x - ovalSize * CLOUD_SIDE_SCALE, y - ovalSize * CLOUD_SIDE_OFFSET),
        size = Size(ovalSize * CLOUD_SIDE_SCALE, ovalSize * CLOUD_SIDE_HEIGHT),
    )
    drawOval(
        color = color,
        topLeft = Offset(x + ovalSize * CLOUD_SIDE_OFFSET, y - ovalSize * CLOUD_SIDE_OFFSET),
        size = Size(ovalSize * CLOUD_SIDE_SCALE, ovalSize * CLOUD_SIDE_HEIGHT),
    )
}

private fun DrawScope.drawStar(color: Color, x: Float, y: Float, elementSize: Float) {
    val outerRadius = elementSize / 2f
    val innerRadius = outerRadius * STAR_INNER_RATIO
    val path = Path()

    for (i in 0 until STAR_POINTS * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * FULL_CIRCLE_DEGREES / (STAR_POINTS * 2) - STAR_ROTATION_OFFSET) * PI / DEGREES_TO_RADIANS
        val px = x + (radius * cos(angle)).toFloat()
        val py = y + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color)
}

private fun DrawScope.drawParticles(particles: List<DreamParticle>, time: Float) {
    particles.forEach { particle ->
        val color = Color(particle.color)
        repeat(particle.count) { index ->
            val seed = index.toFloat() / particle.count
            val xPos = ((seed + time * particle.speed * HALF_ROTATION) % 1f) * size.width
            val yDrift = sin(time * TWO_PI + seed * TWO_PI) * size.height * PARTICLE_DRIFT_RATIO
            val yPos = seed * size.height + yDrift
            val particleSize = particle.size

            when (particle.shape) {
                ParticleShape.DOT -> drawCircle(color = color, radius = particleSize, center = Offset(xPos, yPos))
                ParticleShape.SPARKLE -> drawSparkle(color, xPos, yPos, particleSize)
                ParticleShape.RING -> drawCircle(
                    color = color,
                    radius = particleSize,
                    center = Offset(xPos, yPos),
                    style = Stroke(width = particleSize * RING_STROKE_RATIO),
                )
            }
        }
    }
}

private fun DrawScope.drawSparkle(color: Color, x: Float, y: Float, particleSize: Float) {
    val lineLen = particleSize * SPARKLE_LINE_RATIO
    drawLine(color = color, start = Offset(x - lineLen, y), end = Offset(x + lineLen, y), strokeWidth = 1f)
    drawLine(color = color, start = Offset(x, y - lineLen), end = Offset(x, y + lineLen), strokeWidth = 1f)
}

// region Previews

private const val PREVIEW_HEIGHT = 280

@Suppress("MagicNumber")
private fun previewMysteriousScene() = DreamScene(
    palette = DreamPalette(sky = 0xFF0D1B2A, horizon = 0xFF1B263B, accent = 0xFF415A77),
    layers = listOf(
        DreamLayer(
            depth = 0.2f,
            elements = listOf(
                DreamElement(shape = ElementShape.MOUNTAIN, x = 0.25f, y = 0.7f, scale = 2.5f, color = 0xFF1B263B, alpha = 0.6f),
                DreamElement(shape = ElementShape.MOUNTAIN, x = 0.7f, y = 0.7f, scale = 2.0f, color = 0xFF1B263B, alpha = 0.5f),
            ),
        ),
        DreamLayer(
            depth = 0.5f,
            elements = listOf(
                DreamElement(shape = ElementShape.TREE, x = 0.15f, y = 0.8f, scale = 1.5f, color = 0xFF415A77, alpha = 0.7f),
                DreamElement(shape = ElementShape.CLOUD, x = 0.6f, y = 0.25f, scale = 1.8f, color = 0x80FFFFFF, alpha = 0.4f),
            ),
        ),
        DreamLayer(
            depth = 0.8f,
            elements = listOf(
                DreamElement(shape = ElementShape.STAR, x = 0.3f, y = 0.12f, scale = 0.8f, color = 0xFFE0E1DD, alpha = 0.9f),
                DreamElement(shape = ElementShape.STAR, x = 0.8f, y = 0.08f, scale = 0.6f, color = 0xFFE0E1DD, alpha = 0.7f),
            ),
        ),
    ),
    particles = listOf(
        DreamParticle(shape = ParticleShape.SPARKLE, count = 15, color = 0xCCE0E1DD, speed = 0.8f, size = 3f),
        DreamParticle(shape = ParticleShape.DOT, count = 10, color = 0x80415A77, speed = 0.4f, size = 2f),
    ),
)

@Suppress("MagicNumber")
private fun previewJoyfulScene() = DreamScene(
    palette = DreamPalette(sky = 0xFF87CEEB, horizon = 0xFFFFF8DC, accent = 0xFFFFD700),
    layers = listOf(
        DreamLayer(
            depth = 0.2f,
            elements = listOf(
                DreamElement(shape = ElementShape.WAVE, x = 0.5f, y = 0.85f, scale = 3.0f, color = 0xFF4682B4, alpha = 0.5f),
            ),
        ),
        DreamLayer(
            depth = 0.4f,
            elements = listOf(
                DreamElement(shape = ElementShape.CIRCLE, x = 0.8f, y = 0.15f, scale = 2.0f, color = 0xFFFFD700, alpha = 0.8f),
                DreamElement(shape = ElementShape.CLOUD, x = 0.3f, y = 0.2f, scale = 1.5f, color = 0xCCFFFFFF, alpha = 0.6f),
            ),
        ),
        DreamLayer(
            depth = 0.7f,
            elements = listOf(
                DreamElement(shape = ElementShape.TRIANGLE, x = 0.5f, y = 0.5f, scale = 1.2f, color = 0xFFFF6347, alpha = 0.7f),
            ),
        ),
    ),
    particles = listOf(
        DreamParticle(shape = ParticleShape.RING, count = 8, color = 0x80FFD700, speed = 1.2f, size = 5f),
    ),
)

@Preview(name = "Mysterious")
@Composable
private fun DreamscapeCanvasMysteriousPreview() {
    DreamscapeCanvas(
        scene = previewMysteriousScene(),
        modifier = Modifier
            .fillMaxWidth()
            .height(PREVIEW_HEIGHT.dp),
    )
}

@Preview(name = "Joyful")
@Composable
private fun DreamscapeCanvasJoyfulPreview() {
    DreamscapeCanvas(
        scene = previewJoyfulScene(),
        modifier = Modifier
            .fillMaxWidth()
            .height(PREVIEW_HEIGHT.dp),
    )
}

// endregion
