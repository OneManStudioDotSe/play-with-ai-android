package se.onemanstudio.playaroundwithai.feature.chat.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val NUM_POINTS = 12

enum class AmoebaState {
    IDLE,       // Soft, organic movement
    LOADING,    // Tighter shape, spinning fast
    SPIKY       // Sharp peaks and valleys, faster movement
}

@Composable
fun AmoebaShapeAnimation(
    state: AmoebaState,
    modifier: Modifier = Modifier
) {
    val points = remember { mutableStateListOf<Animatable<Float, AnimationVector1D>>() }
    val rotation = remember { Animatable(0f) }
    val color = MaterialTheme.colorScheme.primary

    // Initialize points if needed
    LaunchedEffect(Unit) {
        if (points.isEmpty()) {
            repeat(NUM_POINTS) {
                points.add(Animatable(1.0f))
            }
        }
    }

    // 1. Rotation Logic (Mainly for Loading state)
    LaunchedEffect(state) {
        if (state == AmoebaState.LOADING) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            // Smoothly stop rotation when leaving loading state
            val currentRotation = rotation.value % 360
            rotation.snapTo(currentRotation)
            rotation.animateTo(
                targetValue = if (currentRotation > 180) 360f else 0f,
                animationSpec = tween(500, easing = LinearOutSlowInEasing)
            )
            rotation.snapTo(0f)
        }
    }

    // 2. Point Animation Logic (Radius expansion/contraction)
    LaunchedEffect(state, points) {
        if (points.isNotEmpty()) {
            points.forEachIndexed { index, point ->
                launch {
                    while (isActive) {
                        // Determine targets based on state
                        val (target, duration) = when (state) {
                            AmoebaState.IDLE -> {
                                // Original logic: random flow
                                val t = 0.7f + Random.nextFloat() * 0.6f
                                val d = Random.nextInt(1500, 3000)
                                t to d
                            }

                            AmoebaState.LOADING -> {
                                // Tighter circle, small ripples
                                val t = 0.9f + Random.nextFloat() * 0.2f
                                val d = Random.nextInt(500, 1000)
                                t to d
                            }

                            AmoebaState.SPIKY -> {
                                // Spikes: Alternating Highs (Odd) and Lows (Even)
                                val isSpike = index % 2 == 0
                                val base = if (isSpike) 1.6f else 0.5f
                                // Add random jitter so it feels alive, not static
                                val t = base + (Random.nextFloat() - 0.5f) * 0.2f
                                val d = Random.nextInt(200, 600) // Fast, aggressive movement
                                t to d
                            }
                        }

                        point.animateTo(
                            targetValue = target,
                            animationSpec = tween(
                                durationMillis = duration,
                                easing = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)
                            )
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(), // Use passed modifier
        contentAlignment = Alignment.Center
    ) {
        if (points.isNotEmpty()) {
            Canvas(modifier = Modifier.size(200.dp)) {
                // Apply rotation for loading state
                rotate(rotation.value) {
                    val path = Path()
                    val angleStep = (2 * PI / NUM_POINTS).toFloat()
                    val radius = size.minDimension / 2.5f

                    // Calculate control points based on current animated values
                    val controlPoints = mutableListOf<Offset>()
                    for (i in 0 until NUM_POINTS) {
                        val angle = i * angleStep
                        // Use correct index for points
                        val pointScale = points[i].value
                        val x = center.x + cos(angle) * radius * pointScale
                        val y = center.y + sin(angle) * radius * pointScale
                        controlPoints.add(Offset(x, y))
                    }

                    if (controlPoints.size > 1) {
                        // Move to first point to start
                        path.moveTo(controlPoints.first().x, controlPoints.first().y)

                        // Draw Catmull-Rom spline through points
                        for (i in 0 until controlPoints.size) {
                            val p0 = controlPoints[(i - 1 + controlPoints.size) % controlPoints.size]
                            val p1 = controlPoints[i]
                            val p2 = controlPoints[(i + 1) % controlPoints.size]
                            val p3 = controlPoints[(i + 2) % controlPoints.size]

                            // Interpolate segments
                            for (t in 1..10) {
                                val tFloat = t / 10f
                                val a = -0.5f * p0 + 1.5f * p1 - 1.5f * p2 + 0.5f * p3
                                val b = p0 - 2.5f * p1 + 2f * p2 - 0.5f * p3
                                val c = -0.5f * p0 + 0.5f * p2

                                // Cubic interpolation logic
                                val nextPoint = a * tFloat * tFloat * tFloat +
                                        b * tFloat * tFloat +
                                        c * tFloat +
                                        p1

                                path.lineTo(nextPoint.x, nextPoint.y)
                            }
                        }
                        path.close()
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = Dimensions.paddingSmall.toPx())
                    )
                }
            }
        }
    }
}

private operator fun Float.times(p: Offset) = Offset(this * p.x, this * p.y)
