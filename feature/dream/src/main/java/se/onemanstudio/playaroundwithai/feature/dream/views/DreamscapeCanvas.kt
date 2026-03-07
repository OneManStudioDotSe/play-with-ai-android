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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamElement
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamLayer
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamParticle
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ElementShape
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape
import timber.log.Timber
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Visual bands for element placement. Elements are classified by shape into non-overlapping
 * vertical zones, rendered back-to-front for correct layering.
 */
private enum class VisualBand { SKY, UPPER, MID, GROUND }

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
            repeatMode = RepeatMode.Reverse,
        ),
        label = "time",
    )
    val slowTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SLOW_ANIMATION_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "slowTime",
    )

    val classified = remember(scene) {
        scene.layers.flatMap { layer ->
            layer.elements.map { Triple(it, layer.depth, visualBandFor(it.shape)) }
        }.sortedWith(compareBy({ it.third.ordinal }, { it.second }))
    }

    LaunchedEffect(scene) {
        Timber.d(
            "DreamCanvas - Scene: %d layers, %d classified elements, %d particle types",
            scene.layers.size, classified.size, scene.particles.size,
        )
        Timber.d("DreamCanvas - Palette: sky=0x%08X, horizon=0x%08X", scene.palette.sky.toInt(), scene.palette.horizon.toInt())
    }

    Canvas(modifier = modifier) {
        //Timber.d("DreamCanvas - DRAW size=%.0fx%.0f", size.width, size.height)
        drawClampedGradient(scene.palette)

        classified.forEach { (element, layerDepth, band) ->
            val speed = parallaxSpeed(band, layerDepth)
            val layerOffset = time * speed * size.width

            if (band == VisualBand.GROUND) {
                drawGroundElement(element, layerOffset, slowTime)
            } else {
                val range = bandYRange(band)
                val y = (range.start + element.y * (range.endInclusive - range.start)) * size.height
                val verticalDrift = sin(slowTime * TWO_PI + element.x * TWO_PI) *
                    size.height * VERTICAL_DRIFT_AMPLITUDE * layerDepth
                drawNonGroundElement(element, layerOffset, y + verticalDrift, slowTime)
            }
        }

        drawParticles(scene.particles, time, slowTime)
    }
}

// region Classification & Helpers

private fun visualBandFor(shape: ElementShape): VisualBand = when (shape) {
    ElementShape.STAR, ElementShape.CRESCENT, ElementShape.AURORA -> VisualBand.SKY
    ElementShape.CLOUD, ElementShape.CIRCLE, ElementShape.DIAMOND, ElementShape.SPIRAL, ElementShape.CRYSTAL -> VisualBand.UPPER
    ElementShape.TRIANGLE -> VisualBand.MID
    ElementShape.MOUNTAIN, ElementShape.TREE, ElementShape.WAVE, ElementShape.LOTUS -> VisualBand.GROUND
}

@Suppress("MagicNumber")
private fun bandYRange(band: VisualBand): ClosedFloatingPointRange<Float> = when (band) {
    VisualBand.SKY -> 0.03f..0.25f
    VisualBand.UPPER -> 0.22f..0.45f
    VisualBand.MID -> 0.42f..0.60f
    VisualBand.GROUND -> 0f..1f // Not used for Y mapping — ground is bottom-anchored
}

@Suppress("MagicNumber")
private fun parallaxSpeed(band: VisualBand, depth: Float): Float = when (band) {
    VisualBand.SKY -> 0.02f + depth * 0.01f
    VisualBand.UPPER -> 0.05f + depth * 0.02f
    VisualBand.MID -> 0.10f + depth * 0.03f
    VisualBand.GROUND -> 0.18f + depth * 0.04f
}

private fun clampBrightness(color: Color, maxLuminance: Float): Color {
    val luminance = LUMINANCE_RED * color.red + LUMINANCE_GREEN * color.green + LUMINANCE_BLUE * color.blue
    if (luminance <= maxLuminance) return color
    val scale = maxLuminance / luminance
    return Color(red = color.red * scale, green = color.green * scale, blue = color.blue * scale, alpha = color.alpha)
}

// endregion

// region Background

private fun DrawScope.drawClampedGradient(palette: DreamPalette) {
    val skyColor = clampBrightness(Color(palette.sky.toInt()), MAX_BACKGROUND_LUMINANCE)
    val horizonColor = clampBrightness(Color(palette.horizon.toInt()), MAX_BACKGROUND_LUMINANCE)
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(skyColor, horizonColor)),
        size = size,
    )
}

// endregion

// region Element Dispatchers

private fun DrawScope.drawNonGroundElement(
    element: DreamElement,
    layerOffset: Float,
    y: Float,
    slowTime: Float,
) {
    val baseX = element.x * size.width
    val offsetX = (baseX + layerOffset) % (size.width * PARALLAX_WRAP) - size.width * PARALLAX_OFFSET
    val elementSize = element.scale * size.width * ELEMENT_SIZE_RATIO
    val baseColor = Color(element.color.toInt()).copy(alpha = element.alpha)
    val slowPhase = slowTime * TWO_PI

    when (element.shape) {
        ElementShape.CIRCLE -> {
            val scale = 1f + sin(slowPhase) * BREATHE_AMPLITUDE
            drawCircle(color = baseColor, radius = elementSize * scale / 2f, center = Offset(offsetX, y))
        }

        ElementShape.TRIANGLE -> drawTriangle(baseColor, offsetX, y, elementSize)

        ElementShape.CLOUD -> {
            val bob = sin(slowPhase) * elementSize * CLOUD_BOB_RATIO
            drawCloud(baseColor, offsetX, y + bob, elementSize)
        }

        ElementShape.STAR -> {
            val rotation = sin(slowPhase) * STAR_ROCK_DEGREES
            drawRotated(rotation, offsetX, y) { drawStar(baseColor, offsetX, y, elementSize) }
        }

        ElementShape.CRESCENT -> {
            val rock = sin(slowPhase) * CRESCENT_ROCK_DEGREES
            drawRotated(rock, offsetX, y) { drawCrescent(baseColor, offsetX, y, elementSize) }
        }

        ElementShape.DIAMOND -> {
            val rotation = sin(slowPhase) * DIAMOND_OSCILLATION_DEGREES
            drawRotated(rotation, offsetX, y) { drawDiamond(baseColor, offsetX, y, elementSize) }
        }

        ElementShape.SPIRAL -> {
            val rotation = sin(slowPhase) * SPIRAL_OSCILLATION_DEGREES
            drawRotated(rotation, offsetX, y) { drawSpiral(baseColor, offsetX, y, elementSize) }
        }

        ElementShape.AURORA -> drawAurora(baseColor, offsetX, y, elementSize, slowTime)

        ElementShape.CRYSTAL -> {
            val shimmer = CRYSTAL_SHIMMER_BASE + sin(slowPhase * CRYSTAL_SHIMMER_FREQ) * CRYSTAL_SHIMMER_RANGE
            val color = baseColor.copy(alpha = baseColor.alpha * shimmer)
            drawCrystal(color, offsetX, y, elementSize)
        }

        else -> {} // Ground shapes handled by drawGroundElement
    }
}

private fun DrawScope.drawGroundElement(
    element: DreamElement,
    layerOffset: Float,
    slowTime: Float,
) {
    val baseX = element.x * size.width
    val offsetX = (baseX + layerOffset) % (size.width * PARALLAX_WRAP) - size.width * PARALLAX_OFFSET
    val elementSize = element.scale * size.width * ELEMENT_SIZE_RATIO
    val baseColor = Color(element.color.toInt()).copy(alpha = element.alpha)
    val upwardOffset = element.y * elementSize * GROUND_Y_VARIETY
    val slowPhase = slowTime * TWO_PI

    when (element.shape) {
        ElementShape.MOUNTAIN -> drawMountainAnchored(baseColor, offsetX, elementSize, upwardOffset)

        ElementShape.TREE -> {
            val sway = sin(slowPhase * 2f) * elementSize * TREE_SWAY_RATIO
            drawTreeAnchored(baseColor, offsetX + sway, elementSize, upwardOffset)
        }

        ElementShape.WAVE -> {
            val phase = sin(slowPhase) * elementSize * WAVE_PHASE_RATIO
            drawWaveAnchored(baseColor, offsetX, elementSize, upwardOffset, phase)
        }

        ElementShape.LOTUS -> {
            val scale = 1f + sin(slowPhase) * BREATHE_AMPLITUDE
            drawLotusAnchored(baseColor, offsetX, elementSize * scale, upwardOffset)
        }

        else -> {} // Non-ground shapes handled by drawNonGroundElement
    }
}

// endregion

private inline fun DrawScope.drawRotated(degrees: Float, pivotX: Float, pivotY: Float, block: DrawScope.() -> Unit) {
    withTransform({ rotate(degrees, Offset(pivotX, pivotY)) }) { block() }
}

// region Element Drawing Functions

private fun DrawScope.drawTriangle(color: Color, x: Float, y: Float, elementSize: Float) {
    val path = Path().apply {
        moveTo(x, y - elementSize / 2f)
        lineTo(x - elementSize / 2f, y + elementSize / 2f)
        lineTo(x + elementSize / 2f, y + elementSize / 2f)
        close()
    }
    drawPath(path, color)
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

private fun DrawScope.drawCrescent(color: Color, x: Float, y: Float, elementSize: Float) {
    val radius = elementSize / 2f
    val path = Path().apply {
        addArc(
            oval = androidx.compose.ui.geometry.Rect(x - radius, y - radius, x + radius, y + radius),
            startAngleDegrees = CRESCENT_START_ANGLE,
            sweepAngleDegrees = CRESCENT_SWEEP_ANGLE,
        )
        val innerR = radius * (1f - CRESCENT_INNER_OFFSET)
        val innerCx = x + radius * CRESCENT_INNER_OFFSET
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(innerCx - innerR, y - innerR, innerCx + innerR, y + innerR),
            startAngleDegrees = CRESCENT_START_ANGLE + CRESCENT_SWEEP_ANGLE,
            sweepAngleDegrees = -CRESCENT_SWEEP_ANGLE,
            forceMoveTo = false,
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawDiamond(color: Color, x: Float, y: Float, elementSize: Float) {
    val halfH = elementSize * DIAMOND_ELONGATION / 2f
    val halfW = elementSize * DIAMOND_WIDTH_RATIO / 2f
    val path = Path().apply {
        moveTo(x, y - halfH)
        lineTo(x + halfW, y)
        lineTo(x, y + halfH)
        lineTo(x - halfW, y)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawSpiral(color: Color, x: Float, y: Float, elementSize: Float) {
    val maxRadius = elementSize / 2f
    val path = Path()
    for (i in 0..SPIRAL_POINTS) {
        val t = i.toFloat() / SPIRAL_POINTS
        val theta = t * SPIRAL_ROTATIONS * TWO_PI
        val r = maxRadius * (E.toFloat().pow(SPIRAL_GROWTH_RATE * theta) - 1f) /
                (E.toFloat().pow(SPIRAL_GROWTH_RATE * SPIRAL_ROTATIONS * TWO_PI) - 1f)
        val px = x + r * cos(theta)
        val py = y + r * sin(theta)
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    drawPath(path, color, style = Stroke(width = elementSize * SPIRAL_STROKE_RATIO))
}

private fun DrawScope.drawLotus(color: Color, x: Float, y: Float, elementSize: Float) {
    val petalLen = elementSize * LOTUS_PETAL_LENGTH
    val petalWidth = elementSize * LOTUS_PETAL_WIDTH
    for (i in 0 until LOTUS_PETALS) {
        val angle = i * FULL_CIRCLE_DEGREES.toFloat() / LOTUS_PETALS
        drawRotated(angle, x, y) {
            val path = Path().apply {
                moveTo(x, y)
                cubicTo(x - petalWidth, y - petalLen * LOTUS_PETAL_CURVE, x - petalWidth * LOTUS_PETAL_TIP, y - petalLen, x, y - petalLen)
                cubicTo(x + petalWidth * LOTUS_PETAL_TIP, y - petalLen, x + petalWidth, y - petalLen * LOTUS_PETAL_CURVE, x, y)
                close()
            }
            drawPath(path, color)
        }
    }
}

private fun DrawScope.drawAurora(color: Color, x: Float, y: Float, elementSize: Float, time: Float) {
    for (i in 0 until AURORA_CURVES) {
        val yOff = (i - AURORA_CURVES / 2f) * elementSize * AURORA_SPACING
        val phase = time * TWO_PI + i * AURORA_PHASE_STEP
        val alpha = (AURORA_BASE_ALPHA - i * AURORA_ALPHA_STEP).coerceAtLeast(AURORA_ALPHA_MIN)
        val strokeW = elementSize * (AURORA_STROKE_MIN + i * AURORA_STROKE_STEP)

        val path = Path().apply {
            moveTo(x - elementSize, y + yOff)
            cubicTo(
                x - elementSize * AURORA_CTRL_X, y + yOff - elementSize * AURORA_CTRL_Y + sin(phase) * elementSize * AURORA_UNDULATION,
                x + elementSize * AURORA_CTRL_X, y + yOff + elementSize * AURORA_CTRL_Y + sin(phase + 1f) * elementSize * AURORA_UNDULATION,
                x + elementSize, y + yOff,
            )
        }
        drawPath(path, color.copy(alpha = alpha), style = Stroke(width = strokeW))
    }
}

private fun DrawScope.drawCrystal(color: Color, x: Float, y: Float, elementSize: Float) {
    val radius = elementSize / 2f
    val path = Path()
    for (i in 0 until CRYSTAL_SIDES) {
        val angle = (i * FULL_CIRCLE_DEGREES / CRYSTAL_SIDES - STAR_ROTATION_OFFSET) * PI / DEGREES_TO_RADIANS
        val px = x + (radius * cos(angle)).toFloat()
        val py = y + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color)

    // Internal facet lines from alternate vertices to center
    for (i in 0 until CRYSTAL_SIDES step 2) {
        val angle = (i * FULL_CIRCLE_DEGREES / CRYSTAL_SIDES - STAR_ROTATION_OFFSET) * PI / DEGREES_TO_RADIANS
        val px = x + (radius * cos(angle)).toFloat()
        val py = y + (radius * sin(angle)).toFloat()
        drawLine(
            color = color.copy(alpha = color.alpha * CRYSTAL_FACET_ALPHA),
            start = Offset(px, py),
            end = Offset(x, y),
            strokeWidth = 1f,
        )
    }
}

// endregion

// region Bottom-Anchored Ground Drawing Functions

private fun DrawScope.drawMountainAnchored(color: Color, x: Float, elementSize: Float, upwardOffset: Float) {
    val peakY = size.height - elementSize * MOUNTAIN_PEAK_FACTOR - upwardOffset
    val halfWidth = elementSize * MOUNTAIN_BASE_HALF_WIDTH
    val path = Path().apply {
        moveTo(x, peakY)
        lineTo(x - halfWidth, size.height)
        lineTo(x + halfWidth, size.height)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawTreeAnchored(color: Color, x: Float, elementSize: Float, upwardOffset: Float) {
    val trunkWidth = elementSize * TREE_TRUNK_WIDTH_RATIO
    val trunkHeight = elementSize * TREE_TRUNK_HEIGHT_RATIO
    val trunkBottom = size.height - upwardOffset
    val trunkTop = trunkBottom - trunkHeight

    drawRect(
        color = color.copy(alpha = color.alpha * TREE_TRUNK_ALPHA),
        topLeft = Offset(x - trunkWidth / 2f, trunkTop),
        size = Size(trunkWidth, trunkHeight),
    )

    val canopyPath = Path().apply {
        moveTo(x, trunkTop - elementSize * TREE_CANOPY_WIDTH_RATIO)
        lineTo(x - elementSize * TREE_CANOPY_WIDTH_RATIO, trunkTop)
        lineTo(x + elementSize * TREE_CANOPY_WIDTH_RATIO, trunkTop)
        close()
    }
    drawPath(canopyPath, color)
}

private fun DrawScope.drawWaveAnchored(
    color: Color,
    x: Float,
    elementSize: Float,
    upwardOffset: Float,
    phaseShift: Float,
) {
    val crestY = size.height - elementSize * WAVE_CREST_HEIGHT - upwardOffset
    val path = Path().apply {
        moveTo(x - elementSize, crestY)
        cubicTo(
            x - elementSize / 2f, crestY - elementSize * WAVE_CONTROL_OFFSET + phaseShift,
            x, crestY + elementSize * WAVE_CONTROL_OFFSET + phaseShift,
            x + elementSize / 2f, crestY,
        )
        cubicTo(
            x + elementSize, crestY - elementSize * WAVE_CONTROL_OFFSET + phaseShift,
            x + elementSize * WAVE_SECOND_CONTROL, crestY + elementSize * WAVE_CONTROL_OFFSET + phaseShift,
            x + elementSize * WAVE_SECOND_END, crestY,
        )
        // Close down to canvas bottom for a filled "water surface"
        lineTo(x + elementSize * WAVE_SECOND_END, size.height)
        lineTo(x - elementSize, size.height)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawLotusAnchored(color: Color, x: Float, elementSize: Float, upwardOffset: Float) {
    val centerY = size.height - elementSize * LOTUS_CENTER_HEIGHT - upwardOffset
    drawLotus(color, x, centerY, elementSize)
}

// endregion

// region Particle Drawing

@Suppress("LongMethod")
private fun DrawScope.drawParticles(particles: List<DreamParticle>, time: Float, slowTime: Float) {
    particles.forEach { particle ->
        val color = Color(particle.color.toInt())
        val effectiveCount = particle.count.coerceAtMost(MAX_PARTICLE_COUNT)
        val usableWidth = size.width * (1f - 2f * PARTICLE_MARGIN)
        val usableHeight = size.height * (1f - 2f * PARTICLE_MARGIN)
        val marginX = size.width * PARTICLE_MARGIN
        val marginY = size.height * PARTICLE_MARGIN

        repeat(effectiveCount) { index ->
            val seed = index.toFloat() / effectiveCount
            val timePhase = time * TWO_PI + seed * TWO_PI
            val slowPhase = slowTime * TWO_PI + seed * TWO_PI

            val baseXPos = marginX + ((seed + time * particle.speed * HALF_ROTATION) % 1f) * usableWidth
            val baseYDrift = sin(timePhase) * size.height * PARTICLE_DRIFT_RATIO
            val baseYPos = marginY + seed * usableHeight + baseYDrift
            val particleSize = particle.size

            when (particle.shape) {
                ParticleShape.DOT -> {
                    val xDrift = sin(timePhase) * size.width * DOT_X_DRIFT
                    val yLissajous = cos(timePhase * DOT_Y_LISSAJOUS_FREQ) * size.height * DOT_Y_LISSAJOUS_AMP
                    drawCircle(color = color, radius = particleSize, center = Offset(baseXPos + xDrift, baseYPos + yLissajous))
                }

                ParticleShape.SPARKLE -> {
                    val alpha = SPARKLE_ALPHA_BASE + sin(timePhase * SPARKLE_FREQ) * SPARKLE_ALPHA_BASE
                    drawSparkle(color.copy(alpha = alpha), baseXPos, baseYPos, particleSize)
                }

                ParticleShape.RING -> {
                    val scale = 1f + sin(timePhase * RING_FREQ) * RING_BREATHE_AMPLITUDE
                    drawCircle(
                        color = color,
                        radius = particleSize * scale,
                        center = Offset(baseXPos, baseYPos),
                        style = Stroke(width = particleSize * RING_STROKE_RATIO),
                    )
                }

                ParticleShape.TEARDROP -> {
                    val yDown = baseYPos + time * usableHeight * TEARDROP_DOWN_BIAS * particle.speed
                    val xSway = baseXPos + sin(slowPhase) * usableWidth * TEARDROP_SWAY_RATIO
                    val wrappedY = marginY + (yDown - marginY) % usableHeight
                    drawTeardrop(color, xSway, wrappedY, particleSize)
                }

                ParticleShape.DIAMOND_MOTE -> {
                    val rotation = slowTime * FULL_CIRCLE_DEGREES.toFloat() + seed * FULL_CIRCLE_DEGREES.toFloat()
                    drawRotated(rotation, baseXPos, baseYPos) {
                        drawDiamondMote(color, baseXPos, baseYPos, particleSize)
                    }
                }

                ParticleShape.DASH -> {
                    val dashX = marginX + ((seed + time * particle.speed * HALF_ROTATION * DASH_SPEED_MULTIPLIER) % 1f) * usableWidth
                    val rotation = sin(timePhase) * DASH_ROTATION_AMP
                    drawRotated(rotation, dashX, baseYPos) {
                        drawDash(color, dashX, baseYPos, particleSize)
                    }
                }

                ParticleShape.STARBURST -> {
                    val alpha = SPARKLE_ALPHA_BASE + sin(timePhase * STARBURST_TWINKLE_SPEED) * SPARKLE_ALPHA_BASE
                    drawStarburst(color.copy(alpha = alpha), baseXPos, baseYPos, particleSize)
                }
            }
        }
    }
}

private fun DrawScope.drawSparkle(color: Color, x: Float, y: Float, particleSize: Float) {
    val lineLen = particleSize * SPARKLE_LINE_RATIO
    drawLine(color = color, start = Offset(x - lineLen, y), end = Offset(x + lineLen, y), strokeWidth = 1f)
    drawLine(color = color, start = Offset(x, y - lineLen), end = Offset(x, y + lineLen), strokeWidth = 1f)
}

private fun DrawScope.drawTeardrop(color: Color, x: Float, y: Float, particleSize: Float) {
    val path = Path().apply {
        moveTo(x, y - particleSize)
        cubicTo(
            x - particleSize * TEARDROP_WIDTH, y,
            x - particleSize * TEARDROP_WIDTH, y + particleSize * TEARDROP_CURVE,
            x, y + particleSize,
        )
        cubicTo(
            x + particleSize * TEARDROP_WIDTH, y + particleSize * TEARDROP_CURVE,
            x + particleSize * TEARDROP_WIDTH, y,
            x, y - particleSize,
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawDiamondMote(color: Color, x: Float, y: Float, particleSize: Float) {
    val path = Path().apply {
        moveTo(x, y - particleSize)
        lineTo(x + particleSize * DIAMOND_MOTE_WIDTH, y)
        lineTo(x, y + particleSize)
        lineTo(x - particleSize * DIAMOND_MOTE_WIDTH, y)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawDash(color: Color, x: Float, y: Float, particleSize: Float) {
    val halfLen = particleSize * DASH_LENGTH_RATIO / 2f
    drawLine(
        color = color,
        start = Offset(x - halfLen, y),
        end = Offset(x + halfLen, y),
        strokeWidth = particleSize * DASH_STROKE_RATIO,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawStarburst(color: Color, x: Float, y: Float, particleSize: Float) {
    val len = particleSize * SPARKLE_LINE_RATIO
    val diagLen = len * STARBURST_DIAGONAL_RATIO
    // Horizontal + Vertical
    drawLine(color = color, start = Offset(x - len, y), end = Offset(x + len, y), strokeWidth = 1f)
    drawLine(color = color, start = Offset(x, y - len), end = Offset(x, y + len), strokeWidth = 1f)
    // Diagonals
    drawLine(color = color, start = Offset(x - diagLen, y - diagLen), end = Offset(x + diagLen, y + diagLen), strokeWidth = 1f)
    drawLine(color = color, start = Offset(x + diagLen, y - diagLen), end = Offset(x - diagLen, y + diagLen), strokeWidth = 1f)
}

// endregion

// region Previews

@Suppress("MagicNumber")
private fun previewMysteriousScene() = DreamScene(
    palette = DreamPalette(sky = 0xFF0D1B2A, horizon = 0xFF1B263B, accent = 0xFF415A77),
    layers = listOf(
        DreamLayer(
            depth = 0.8f,
            elements = listOf(
                DreamElement(shape = ElementShape.STAR, x = 0.3f, y = 0.3f, scale = 0.8f, color = 0xFFE0E1DD, alpha = 0.9f),
                DreamElement(shape = ElementShape.STAR, x = 0.8f, y = 0.15f, scale = 0.6f, color = 0xFFE0E1DD, alpha = 0.7f),
                DreamElement(shape = ElementShape.CRESCENT, x = 0.7f, y = 0.3f, scale = 1.8f, color = 0xFFE0E1DD, alpha = 0.6f),
            ),
        ),
        DreamLayer(
            depth = 0.5f,
            elements = listOf(
                DreamElement(shape = ElementShape.AURORA, x = 0.5f, y = 0.5f, scale = 2.0f, color = 0xFF415A77, alpha = 0.4f),
                DreamElement(shape = ElementShape.CRYSTAL, x = 0.75f, y = 0.5f, scale = 1.5f, color = 0xFF415A77, alpha = 0.5f),
            ),
        ),
        DreamLayer(
            depth = 0.2f,
            elements = listOf(
                DreamElement(shape = ElementShape.MOUNTAIN, x = 0.25f, y = 0.5f, scale = 2.5f, color = 0xFF1B263B, alpha = 0.6f),
                DreamElement(shape = ElementShape.TREE, x = 0.15f, y = 0.5f, scale = 1.5f, color = 0xFF415A77, alpha = 0.7f),
            ),
        ),
    ),
    particles = listOf(
        DreamParticle(shape = ParticleShape.STARBURST, count = 10, color = 0xCCE0E1DD, speed = 0.8f, size = 3f),
        DreamParticle(shape = ParticleShape.DIAMOND_MOTE, count = 8, color = 0x80415A77, speed = 0.4f, size = 2.5f),
    ),
)

@Suppress("MagicNumber")
private fun previewJoyfulScene() = DreamScene(
    palette = DreamPalette(sky = 0xFF87CEEB, horizon = 0xFFFFF8DC, accent = 0xFFFFD700),
    layers = listOf(
        DreamLayer(
            depth = 0.4f,
            elements = listOf(
                DreamElement(shape = ElementShape.CIRCLE, x = 0.8f, y = 0.3f, scale = 2.0f, color = 0xFFFFD700, alpha = 0.8f),
                DreamElement(shape = ElementShape.CLOUD, x = 0.3f, y = 0.4f, scale = 1.5f, color = 0xCCFFFFFF, alpha = 0.6f),
            ),
        ),
        DreamLayer(
            depth = 0.7f,
            elements = listOf(
                DreamElement(shape = ElementShape.DIAMOND, x = 0.5f, y = 0.5f, scale = 1.2f, color = 0xFFFF6347, alpha = 0.7f),
                DreamElement(shape = ElementShape.SPIRAL, x = 0.15f, y = 0.4f, scale = 1.0f, color = 0xFFFFD700, alpha = 0.5f),
            ),
        ),
        DreamLayer(
            depth = 0.2f,
            elements = listOf(
                DreamElement(shape = ElementShape.WAVE, x = 0.5f, y = 0.5f, scale = 3.0f, color = 0xFF4682B4, alpha = 0.5f),
                DreamElement(shape = ElementShape.LOTUS, x = 0.3f, y = 0.4f, scale = 1.2f, color = 0xFFFF69B4, alpha = 0.6f),
            ),
        ),
    ),
    particles = listOf(
        DreamParticle(shape = ParticleShape.TEARDROP, count = 8, color = 0x804682B4, speed = 0.6f, size = 3f),
        DreamParticle(shape = ParticleShape.RING, count = 6, color = 0x80FFD700, speed = 1.2f, size = 5f),
    ),
)

@Preview(name = "Mysterious")
@Composable
private fun DreamscapeCanvasMysteriousPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            DreamscapeCanvas(
                scene = previewMysteriousScene(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PREVIEW_HEIGHT.dp),
            )
        }
    }
}

@Preview(name = "Joyful")
@Composable
private fun DreamscapeCanvasJoyfulPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            DreamscapeCanvas(
                scene = previewJoyfulScene(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PREVIEW_HEIGHT.dp),
            )
        }
    }
}

// endregion
