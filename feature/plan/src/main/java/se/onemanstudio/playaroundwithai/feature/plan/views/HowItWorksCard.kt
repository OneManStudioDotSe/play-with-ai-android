package se.onemanstudio.playaroundwithai.feature.plan.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.feature.plan.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HowItWorksCard(modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    NeoBrutalCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = null,
                    tint = electricBlue,
                    modifier = Modifier.size(Dimensions.iconSizeMedium),
                )
                Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
                Text(
                    text = stringResource(R.string.plan_how_it_works_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(
                        if (expanded) R.string.plan_how_it_works_collapse else R.string.plan_how_it_works_expand
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
                Column {
                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

                    Text(
                        text = stringResource(R.string.plan_how_it_works_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

                    Text(
                        text = stringResource(R.string.plan_how_it_works_tools_label),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall),
                    ) {
                        NeoBrutalChip(text = "search_places", onClick = {})
                        NeoBrutalChip(text = "calculate_route", onClick = {})
                    }

                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

                    Text(
                        text = stringResource(R.string.plan_how_it_works_flow),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Preview(name = "HowItWorks Light")
@Composable
private fun HowItWorksCardLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            HowItWorksCard(modifier = Modifier.padding(Dimensions.paddingLarge))
        }
    }
}

@Preview(name = "HowItWorks Dark")
@Composable
private fun HowItWorksCardDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            HowItWorksCard(modifier = Modifier.padding(Dimensions.paddingLarge))
        }
    }
}
