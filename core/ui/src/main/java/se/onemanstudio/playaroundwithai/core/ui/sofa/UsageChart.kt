package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import java.text.NumberFormat

private const val CHART_HEIGHT = 120
private const val DETAIL_TEXT_HEIGHT = 20
private const val BAR_CORNER_RADIUS = 4f
private const val BAR_GAP_FRACTION = 0.3f
private const val WEEKDAY_COUNT = 7
private const val ENTRY_ANIMATION_DURATION_MS = 600
private const val STAGGER_DELAY_MS = 80
private const val FILL_ANIMATION_DURATION_MS = 350
private const val FILL_COMPLETE_THRESHOLD = 0.99f
private const val COUNT_ANIMATION_DURATION_MS = 800
private const val ZERO_BAR_DASH_HEIGHT_DP = 3f
private const val ZERO_BAR_DASH_INTERVAL = 6f
private const val ZERO_BAR_DASH_PHASE = 0f

@Immutable
data class ChartBarData(
    val label: String,
    val value: Float,
    val detailText: String,
)

@Composable
fun UsageChart(
    bars: List<ChartBarData>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    onBarTapped: (Int) -> Unit = {},
) {
    require(bars.size == WEEKDAY_COUNT) { "UsageChart expects exactly $WEEKDAY_COUNT bars" }

    val maxValue = bars.maxOf { it.value }.coerceAtLeast(1f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val dimAlpha = Alphas.extraLow

    // Entry animation (staggered bar growth on first render)
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationStarted = true }

    val entryFractions = bars.mapIndexed { index, _ ->
        animateFloatAsState(
            targetValue = if (animationStarted) 1f else 0f,
            animationSpec = tween(
                durationMillis = ENTRY_ANIMATION_DURATION_MS,
                delayMillis = index * STAGGER_DELAY_MS,
            ),
            label = "entry_$index",
        )
    }

    // Per-bar selection fill animation (0 = unselected, 1 = fully filled with tertiary color)
    val selectionFills = bars.indices.map { index ->
        animateFloatAsState(
            targetValue = if (selectedIndex == index) 1f else 0f,
            animationSpec = tween(durationMillis = FILL_ANIMATION_DURATION_MS),
            label = "fill_$index",
        )
    }

    // Pop animation for the detail text: triggers when the selected bar's fill is complete
    val fillComplete by remember(selectedIndex) {
        derivedStateOf {
            selectedIndex != null &&
                selectedIndex in bars.indices &&
                selectionFills[selectedIndex].value >= FILL_COMPLETE_THRESHOLD
        }
    }

    val popScale by animateFloatAsState(
        targetValue = if (fillComplete) 1f else 0f,
        animationSpec = if (fillComplete) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            tween(durationMillis = 0)
        },
        label = "pop",
    )

    // Counting animation for token number (like battery level animation)
    val countAnimatable = remember { Animatable(0f) }
    val numberFormat = remember { NumberFormat.getIntegerInstance() }

    LaunchedEffect(selectedIndex) {
        countAnimatable.snapTo(0f)
    }

    LaunchedEffect(fillComplete, selectedIndex) {
        if (fillComplete && selectedIndex != null && selectedIndex in bars.indices) {
            countAnimatable.animateTo(
                targetValue = bars[selectedIndex].value,
                animationSpec = tween(
                    durationMillis = COUNT_ANIMATION_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    Column(modifier = modifier) {
        // Always reserve space for detail text to prevent UI jumping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DETAIL_TEXT_HEIGHT.dp)
                .padding(bottom = Dimensions.paddingSmall),
            contentAlignment = Alignment.Center,
        ) {
            if (selectedIndex != null && selectedIndex in bars.indices && popScale > 0f) {
                Text(
                    text = numberFormat.format(countAnimatable.value.toLong()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = popScale
                            scaleY = popScale
                        },
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT.dp)
                .pointerInput(bars) {
                    detectTapGestures { offset ->
                        val barWidth = size.width.toFloat() / WEEKDAY_COUNT
                        val tappedIndex = (offset.x / barWidth).toInt().coerceIn(0, WEEKDAY_COUNT - 1)
                        onBarTapped(tappedIndex)
                    }
                }
        ) {
            val barWidth = size.width / WEEKDAY_COUNT
            val barInnerWidth = barWidth * (1f - BAR_GAP_FRACTION)
            val gap = barWidth * BAR_GAP_FRACTION / 2f

            bars.forEachIndexed { index, bar ->
                val entryFraction = entryFractions[index].value
                val barHeight = (bar.value / maxValue) * size.height * entryFraction
                val x = index * barWidth + gap
                val y = size.height - barHeight

                // Base color: dimmed when another bar is selected, primary otherwise
                val baseColor = when {
                    selectedIndex != null && selectedIndex != index -> primaryColor.copy(alpha = dimAlpha)
                    else -> primaryColor
                }

                // Zero-value indicator: dashed line at the baseline
                if (barHeight <= 0f) {
                    val dashHeight = ZERO_BAR_DASH_HEIGHT_DP.dp.toPx()
                    val dashY = size.height - dashHeight / 2f
                    drawLine(
                        color = baseColor,
                        start = Offset(x, dashY),
                        end = Offset(x + barInnerWidth, dashY),
                        strokeWidth = dashHeight,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(ZERO_BAR_DASH_INTERVAL, ZERO_BAR_DASH_INTERVAL),
                            phase = ZERO_BAR_DASH_PHASE,
                        ),
                    )
                }

                // Draw the full bar in base color
                drawRoundRect(
                    color = baseColor,
                    topLeft = Offset(x, y),
                    size = Size(barInnerWidth, barHeight),
                    cornerRadius = CornerRadius(BAR_CORNER_RADIUS, BAR_CORNER_RADIUS),
                )

                // Overdraw selection fill from the bottom rising upward
                val fillFraction = selectionFills[index].value
                if (fillFraction > 0f) {
                    val fillHeight = barHeight * fillFraction
                    val fillY = y + barHeight - fillHeight
                    drawRoundRect(
                        color = tertiaryColor,
                        topLeft = Offset(x, fillY),
                        size = Size(barInnerWidth, fillHeight),
                        cornerRadius = CornerRadius(BAR_CORNER_RADIUS, BAR_CORNER_RADIUS),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimensions.paddingSmall),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            bars.forEachIndexed { index, bar ->
                val labelColor = if (selectedIndex == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private val previewBars = listOf(
    ChartBarData(label = "M", value = 120f, detailText = "120 tokens"),
    ChartBarData(label = "T", value = 85f, detailText = "85 tokens"),
    ChartBarData(label = "W", value = 200f, detailText = "200 tokens"),
    ChartBarData(label = "T", value = 0f, detailText = "0 tokens"),
    ChartBarData(label = "F", value = 150f, detailText = "150 tokens"),
    ChartBarData(label = "S", value = 45f, detailText = "45 tokens"),
    ChartBarData(label = "S", value = 90f, detailText = "90 tokens"),
)

@Preview(name = "Light")
@Composable
private fun UsageChartLightPreview() {
    SofaAiTheme {
        UsageChart(
            bars = previewBars,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
        )
    }
}

@Preview(name = "Dark")
@Composable
private fun UsageChartDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        UsageChart(
            bars = previewBars,
            selectedIndex = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
        )
    }
}
