package se.onemanstudio.playaroundwithai.feature.plan.views

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.AGENT_PULSE_DOT_COUNT
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.AGENT_PULSE_DOT_RADIUS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.AGENT_PULSE_DURATION_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.AGENT_PULSE_LINE_WIDTH
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.AGENT_PULSE_SIZE
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.AGENT_PULSE_STAGGER_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.PULSE_ALPHA_MAX
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.PULSE_ALPHA_MIN

private val dotColors = listOf(electricBlue, energeticOrange, zestyLime)

@Composable
internal fun AgentPulse(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "agentPulse")
    val lineColor = MaterialTheme.colorScheme.onSurface

    val alphas = List(AGENT_PULSE_DOT_COUNT) { index ->
        val alpha by transition.animateFloat(
            initialValue = PULSE_ALPHA_MIN,
            targetValue = PULSE_ALPHA_MAX,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = AGENT_PULSE_DURATION_MS,
                    delayMillis = index * AGENT_PULSE_STAGGER_MS,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "dotAlpha_$index",
        )
        alpha
    }

    NeoBrutalCard(modifier = modifier) {
        Canvas(modifier = Modifier.size(AGENT_PULSE_SIZE.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val dotSpacing = canvasWidth / (AGENT_PULSE_DOT_COUNT + 1)
            val centerY = canvasHeight / 2f

            for (i in 0 until AGENT_PULSE_DOT_COUNT) {
                val cx = dotSpacing * (i + 1)
                val color = dotColors[i % dotColors.size]

                drawCircle(
                    color = color.copy(alpha = alphas[i]),
                    radius = AGENT_PULSE_DOT_RADIUS,
                    center = Offset(cx, centerY),
                )

                if (i < AGENT_PULSE_DOT_COUNT - 1) {
                    val nextCx = dotSpacing * (i + 2)
                    drawLine(
                        color = lineColor.copy(alpha = alphas[i] * 0.5f),
                        start = Offset(cx + AGENT_PULSE_DOT_RADIUS, centerY),
                        end = Offset(nextCx - AGENT_PULSE_DOT_RADIUS, centerY),
                        strokeWidth = AGENT_PULSE_LINE_WIDTH,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Preview(name = "AgentPulse Light")
@Composable
private fun AgentPulseLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            Row(
                modifier = Modifier.size(120.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgentPulse(modifier = Modifier.size(AGENT_PULSE_SIZE.dp))
            }
        }
    }
}

@Preview(name = "AgentPulse Dark")
@Composable
private fun AgentPulseDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            Row(
                modifier = Modifier.size(120.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgentPulse(modifier = Modifier.size(AGENT_PULSE_SIZE.dp))
            }
        }
    }
}
