package se.onemanstudio.playaroundwithai.feature.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.BOUNCE_AMPLITUDE
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.BOUNCE_DOT_COUNT
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.BOUNCE_DURATION_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.BOUNCE_STAGGER_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.PULSE_ALPHA_MAX
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.PULSE_ALPHA_MIN
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.PULSE_DURATION_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.STEP_BORDER_WIDTH
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.STEP_STAGGER_DELAY_MS
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanStepUi
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState
import se.onemanstudio.playaroundwithai.feature.plan.states.StepIcon

@Composable
internal fun RunningState(state: PlanUiState.Running) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AgentPulse(modifier = Modifier.size(PlanConstants.AGENT_PULSE_SIZE.dp))

        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

        MarkerText(
            text = stringResource(R.string.plan_planning_trip),
            lineColor = energeticOrange,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

        NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                state.steps.forEachIndexed { index, step ->
                    val isLast = index == state.steps.size - 1
                    val transitionState = remember(step) {
                        MutableTransitionState(false).apply { targetState = true }
                    }

                    AnimatedVisibility(
                        visibleState = transitionState,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * STEP_STAGGER_DELAY_MS,
                            )
                        ) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * STEP_STAGGER_DELAY_MS,
                            ),
                        ),
                    ) {
                        StepRow(step = step, isPulsing = isLast)
                    }

                    if (!isLast) {
                        Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(step: PlanStepUi, isPulsing: Boolean) {
    val alpha = if (isPulsing) {
        val transition = rememberInfiniteTransition(label = "pulse")
        val animatedAlpha by transition.animateFloat(
            initialValue = PULSE_ALPHA_MIN,
            targetValue = PULSE_ALPHA_MAX,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = PULSE_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseAlpha",
        )
        animatedAlpha
    } else {
        PULSE_ALPHA_MAX
    }

    val showBouncingDots = isPulsing && step.label.endsWith("...")
    val displayLabel = if (showBouncingDots) step.label.removeSuffix("...") else step.label

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(STEP_BORDER_WIDTH)
                .height(Dimensions.iconSizeLarge)
                .background(stepBorderColor(step.icon)),
        )
        Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
        Icon(
            imageVector = stepIcon(step.icon),
            contentDescription = stepIconContentDescription(step.icon),
            tint = stepIconColor(step.icon),
            modifier = Modifier.size(Dimensions.iconSizeLarge),
        )
        Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (showBouncingDots) {
                    BouncingDots()
                }
            }
            if (step.toolName != null && step.icon == StepIcon.TOOL_CALL) {
                Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                NeoBrutalChip(
                    text = step.toolName,
                    onClick = {},
                )
            }
            if (step.detail != null && step.icon == StepIcon.TOOL_RESULT) {
                Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                Text(
                    text = step.detail,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun BouncingDots() {
    val transition = rememberInfiniteTransition(label = "bouncingDots")

    Row {
        repeat(BOUNCE_DOT_COUNT) { index ->
            val offset by transition.animateFloat(
                initialValue = 0f,
                targetValue = -1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = BOUNCE_DURATION_MS,
                        delayMillis = index * BOUNCE_STAGGER_MS,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bounce_$index",
            )

            val animatedOffset: Dp by animateDpAsState(
                targetValue = BOUNCE_AMPLITUDE * offset,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "dpOffset_$index",
            )

            Text(
                text = ".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.offset(y = animatedOffset),
            )
        }
    }
}

@Composable
private fun stepIcon(icon: StepIcon): ImageVector = when (icon) {
    StepIcon.THINKING -> Icons.Rounded.Psychology
    StepIcon.TOOL_CALL -> Icons.Rounded.Construction
    StepIcon.TOOL_RESULT -> Icons.Rounded.CheckCircle
}

@Composable
private fun stepIconColor(icon: StepIcon) = when (icon) {
    StepIcon.THINKING -> MaterialTheme.colorScheme.primary
    StepIcon.TOOL_CALL -> MaterialTheme.colorScheme.tertiary
    StepIcon.TOOL_RESULT -> MaterialTheme.colorScheme.secondary
}

@Composable
private fun stepBorderColor(icon: StepIcon): Color = when (icon) {
    StepIcon.THINKING -> electricBlue
    StepIcon.TOOL_CALL -> energeticOrange
    StepIcon.TOOL_RESULT -> zestyLime
}

@Composable
private fun stepIconContentDescription(icon: StepIcon) = when (icon) {
    StepIcon.THINKING -> stringResource(R.string.plan_icon_step_thinking)
    StepIcon.TOOL_CALL -> stringResource(R.string.plan_icon_step_tool_call)
    StepIcon.TOOL_RESULT -> stringResource(R.string.plan_icon_step_tool_result)
}

@Preview(name = "Running Light")
@Composable
private fun RunningStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            RunningState(
                state = PlanUiState.Running(
                    steps = persistentListOf(
                        PlanStepUi(icon = StepIcon.THINKING, label = "Planning your trip..."),
                        PlanStepUi(
                            icon = StepIcon.TOOL_CALL,
                            label = "Searching for specialty coffee shops",
                            toolName = "search_places",
                        ),
                        PlanStepUi(
                            icon = StepIcon.TOOL_RESULT,
                            label = "Found 4 coffee shops",
                            toolName = "search_places",
                            detail = "Found 4 coffee shops",
                        ),
                        PlanStepUi(icon = StepIcon.THINKING, label = "Looking for more places..."),
                    ),
                    currentAction = "Thinking",
                ),
            )
        }
    }
}

@Preview(name = "Running Dark")
@Composable
private fun RunningStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            RunningState(
                state = PlanUiState.Running(
                    steps = persistentListOf(
                        PlanStepUi(icon = StepIcon.THINKING, label = "Planning your trip..."),
                        PlanStepUi(
                            icon = StepIcon.TOOL_CALL,
                            label = "Calculating optimal route",
                            toolName = "calculate_route",
                        ),
                    ),
                    currentAction = "Calculating",
                ),
            )
        }
    }
}
