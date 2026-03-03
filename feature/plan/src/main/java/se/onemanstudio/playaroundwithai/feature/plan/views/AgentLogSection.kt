package se.onemanstudio.playaroundwithai.feature.plan.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.STEP_BORDER_WIDTH
import se.onemanstudio.playaroundwithai.feature.plan.R
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanStepUi
import se.onemanstudio.playaroundwithai.feature.plan.states.StepIcon

@Composable
internal fun AgentLogSection(
    steps: PersistentList<PlanStepUi>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MarkerText(
                text = stringResource(R.string.plan_agent_log_title),
                lineColor = energeticOrange,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = stringResource(
                    if (expanded) R.string.plan_agent_log_collapse else R.string.plan_agent_log_expand
                ),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(Dimensions.iconSizeMedium),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            NeoBrutalCard(modifier = Modifier.fillMaxWidth().padding(top = Dimensions.paddingMedium)) {
                Column(modifier = Modifier.padding(Dimensions.paddingMedium)) {
                    steps.forEachIndexed { index, step ->
                        LogStepRow(step = step)
                        if (index < steps.size - 1) {
                            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogStepRow(step: PlanStepUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(STEP_BORDER_WIDTH)
                .height(Dimensions.iconSizeMedium)
                .background(logStepBorderColor(step.icon)),
        )
        Spacer(modifier = Modifier.width(Dimensions.paddingSmall))
        Icon(
            imageVector = logStepIcon(step.icon),
            contentDescription = null,
            tint = logStepIconTint(step.icon),
            modifier = Modifier.size(Dimensions.iconSizeSmall),
        )
        Spacer(modifier = Modifier.width(Dimensions.paddingSmall))
        Text(
            text = step.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (step.toolName != null) {
            Spacer(modifier = Modifier.width(Dimensions.paddingSmall))
            NeoBrutalChip(text = step.toolName, onClick = {})
        }
    }
}

private fun logStepBorderColor(icon: StepIcon) = when (icon) {
    StepIcon.THINKING -> electricBlue
    StepIcon.TOOL_CALL -> energeticOrange
    StepIcon.TOOL_RESULT -> zestyLime
}

private fun logStepIcon(icon: StepIcon): ImageVector = when (icon) {
    StepIcon.THINKING -> Icons.Rounded.Psychology
    StepIcon.TOOL_CALL -> Icons.Rounded.Construction
    StepIcon.TOOL_RESULT -> Icons.Rounded.CheckCircle
}

private fun logStepIconTint(icon: StepIcon) = when (icon) {
    StepIcon.THINKING -> electricBlue
    StepIcon.TOOL_CALL -> energeticOrange
    StepIcon.TOOL_RESULT -> zestyLime
}

@Preview(name = "AgentLog Light")
@Composable
private fun AgentLogSectionLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            AgentLogSection(
                steps = persistentListOf(
                    PlanStepUi(icon = StepIcon.THINKING, label = "Understanding your request..."),
                    PlanStepUi(icon = StepIcon.TOOL_CALL, label = "Searching for coffee shops", toolName = "search_places"),
                    PlanStepUi(
                        icon = StepIcon.TOOL_RESULT,
                        label = "Found 4 places",
                        toolName = "search_places",
                        detail = "Found 4 places",
                    ),
                    PlanStepUi(icon = StepIcon.TOOL_CALL, label = "Calculating route", toolName = "calculate_route"),
                    PlanStepUi(
                        icon = StepIcon.TOOL_RESULT,
                        label = "Route: 2.4 km",
                        toolName = "calculate_route",
                        detail = "Route: 2.4 km",
                    ),
                ),
                modifier = Modifier.padding(Dimensions.paddingLarge),
            )
        }
    }
}

@Preview(name = "AgentLog Dark")
@Composable
private fun AgentLogSectionDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            AgentLogSection(
                steps = persistentListOf(
                    PlanStepUi(icon = StepIcon.THINKING, label = "Understanding your request..."),
                    PlanStepUi(icon = StepIcon.TOOL_CALL, label = "Searching for coffee shops", toolName = "search_places"),
                    PlanStepUi(
                        icon = StepIcon.TOOL_RESULT,
                        label = "Found 4 places",
                        toolName = "search_places",
                        detail = "Found 4 places",
                    ),
                ),
                modifier = Modifier.padding(Dimensions.paddingLarge),
            )
        }
    }
}
