package se.onemanstudio.playaroundwithai.core.ui.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val NUM_POINTS = 12

@Suppress("MagicNumber")
@Composable
fun AmoebaShapeAnimation() {
    val points = remember { mutableStateListOf<Animatable<Float, AnimationVector1D>>() }
    val color = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        if (points.isEmpty()) {
            (0 until NUM_POINTS * 2).forEach { _ ->
                points.add(Animatable(1.0f))
            }
        }
    }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            points.forEach { point ->
                launch {
                    point.animateTo(
                        targetValue = 0.7f + Random.nextFloat() * 0.6f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                // Faster duration: from 0.8 to 2 seconds
                                durationMillis = Random.nextInt(800, 2000),
                                easing = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)
                            ),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (points.isNotEmpty()) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val path = Path()
                val angleStep = (2 * PI / NUM_POINTS).toFloat()
                val radius = size.minDimension / 2.5f

                val controlPoints = mutableListOf<Offset>()
                for (i in 0 until NUM_POINTS) {
                    val angle = i * angleStep
                    val x = center.x + cos(angle) * radius * points[i * 2].value
                    val y = center.y + sin(angle) * radius * points[i * 2 + 1].value
                    controlPoints.add(Offset(x, y))
                }

                if (controlPoints.size > 1) {
                    path.moveTo(controlPoints.first().x, controlPoints.first().y)

                    for (i in 0 until controlPoints.size) {
                        val p0 = controlPoints[(i - 1 + controlPoints.size) % controlPoints.size]
                        val p1 = controlPoints[i]
                        val p2 = controlPoints[(i + 1) % controlPoints.size]
                        val p3 = controlPoints[(i + 2) % controlPoints.size]

                        for (t in 1..10) {
                            val tFloat = t / 10f
                            val a = -0.5f * p0 + 1.5f * p1 - 1.5f * p2 + 0.5f * p3
                            val b = p0 - 2.5f * p1 + 2f * p2 - 0.5f * p3
                            val c = -0.5f * p0 + 0.5f * p2

                            val nextPoint = a * tFloat * tFloat * tFloat + b * tFloat * tFloat + c * tFloat + p1
                            path.lineTo(nextPoint.x, nextPoint.y)
                        }
                    }
                    path.close()
                }


                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
    }
}

private operator fun Float.times(p: Offset) = Offset(this * p.x, this * p.y)

@Preview(showBackground = true)
@Composable
internal fun AmoebaAnimationPreview() {
    AmoebaShapeAnimation()
}
