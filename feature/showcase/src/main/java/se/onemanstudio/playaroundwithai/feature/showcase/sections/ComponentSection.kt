@file:Suppress("TooManyFunctions", "MagicNumber")

package se.onemanstudio.playaroundwithai.feature.showcase.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import se.onemanstudio.playaroundwithai.core.ui.sofa.ChartBarData
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButtonSmall
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalSegmentedButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTextField
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.sofa.UsageChart
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.core.ui.theme.solarYellow
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.feature.showcase.R


@Composable
fun ComponentSection() {
    MarkerText(text = stringResource(R.string.section_components), lineColor = zestyLime)

    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

    CardDemo()
    ComponentSpacer()
    ButtonDemo()
    ComponentSpacer()
    IconButtonDemo()
    ComponentSpacer()
    IconButtonSmallDemo()
    ComponentSpacer()
    TextFieldDemo()
    ComponentSpacer()
    ChipDemo()
    ComponentSpacer()
    SegmentedButtonDemo()
    ComponentSpacer()
    TopAppBarDemo()
    ComponentSpacer()
    MarkerTextDemo()
    ComponentSpacer()
    UsageChartDemo()
}

@Composable
private fun ComponentSpacer() {
    Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
}

@Composable
private fun ComponentLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
}

@Composable
private fun CardDemo() {
    ComponentLabel("NeoBrutalCard")
    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Text(
                text = stringResource(R.string.demo_card_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.demo_card_body),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ButtonDemo() {
    ComponentLabel("NeoBrutalButton")
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
        NeoBrutalButton(
            text = stringResource(R.string.demo_button_enabled),
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
        NeoBrutalButton(
            text = stringResource(R.string.demo_button_with_icon),
            icon = Icons.AutoMirrored.Filled.Send,
            iconContentDescription = stringResource(R.string.ic_send),
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
        NeoBrutalButton(
            text = stringResource(R.string.demo_button_disabled),
            enabled = false,
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun IconButtonDemo() {
    ComponentLabel("NeoBrutalIconButton")
    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
        NeoBrutalIconButton(
            imageVector = Icons.Default.Star,
            contentDescription = stringResource(R.string.ic_star),
            backgroundColor = electricBlue,
            onClick = {}
        )
        NeoBrutalIconButton(
            imageVector = Icons.Default.Favorite,
            contentDescription = stringResource(R.string.ic_favorite),
            backgroundColor = vividPink,
            onClick = {}
        )
        NeoBrutalIconButton(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = stringResource(R.string.ic_thumb_up),
            backgroundColor = zestyLime,
            onClick = {}
        )
    }
}

@Composable
private fun IconButtonSmallDemo() {
    ComponentLabel("NeoBrutalIconButtonSmall")
    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
        NeoBrutalIconButtonSmall(
            imageVector = Icons.Default.Star,
            contentDescription = stringResource(R.string.ic_star),
            onClick = {}
        )
        NeoBrutalIconButtonSmall(
            imageVector = Icons.Default.Favorite,
            contentDescription = stringResource(R.string.ic_favorite),
            onClick = {}
        )
        NeoBrutalIconButtonSmall(
            imageVector = Icons.Default.Share,
            contentDescription = stringResource(R.string.ic_share),
            onClick = {}
        )
    }
}

@Composable
private fun TextFieldDemo() {
    ComponentLabel("NeoBrutalTextField")
    var textValue by remember { mutableStateOf(TextFieldValue("")) }
    NeoBrutalTextField(
        value = textValue,
        onValueChange = { textValue = it },
        placeholder = stringResource(R.string.demo_text_field_placeholder),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ChipDemo() {
    ComponentLabel("NeoBrutalChip")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
        NeoBrutalChip(text = stringResource(R.string.demo_chip_design), onClick = {})
        NeoBrutalChip(text = stringResource(R.string.demo_chip_system), onClick = {})
        NeoBrutalChip(text = stringResource(R.string.demo_chip_sofa), onClick = {})
    }
}

@Composable
private fun SegmentedButtonDemo() {
    ComponentLabel("NeoBrutalSegmentedButton")
    var selectedIndex by remember { mutableIntStateOf(0) }
    NeoBrutalSegmentedButton(
        labels = listOf(
            stringResource(R.string.demo_segment_first),
            stringResource(R.string.demo_segment_second),
            stringResource(R.string.demo_segment_third)
        ),
        selectedIndex = selectedIndex,
        onSelected = { selectedIndex = it },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TopAppBarDemo() {
    ComponentLabel("NeoBrutalTopAppBar")
    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        NeoBrutalTopAppBar(
            title = stringResource(R.string.demo_top_bar_title),
            actions = {
                NeoBrutalIconButtonSmall(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.ic_settings),
                    onClick = {}
                )
            }
        )
    }
}

@Composable
private fun MarkerTextDemo() {
    ComponentLabel("MarkerText")
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
        MarkerText(text = stringResource(R.string.color_electric_blue), lineColor = electricBlue)
        MarkerText(text = stringResource(R.string.color_vivid_pink), lineColor = vividPink)
        MarkerText(text = stringResource(R.string.color_zesty_lime), lineColor = zestyLime)
        MarkerText(text = stringResource(R.string.color_solar_yellow), lineColor = solarYellow)
        MarkerText(text = stringResource(R.string.color_energetic_orange), lineColor = energeticOrange)
    }
}

@Composable
private fun UsageChartDemo() {
    ComponentLabel("UsageChart")
    val sampleChartBars = listOf(
        ChartBarData(label = stringResource(R.string.chart_day_mon), value = 5f, detailText = stringResource(R.string.chart_detail_prompts, 5)),
        ChartBarData(label = stringResource(R.string.chart_day_tue), value = 12f, detailText = stringResource(R.string.chart_detail_prompts, 12)),
        ChartBarData(label = stringResource(R.string.chart_day_wed), value = 8f, detailText = stringResource(R.string.chart_detail_prompts, 8)),
        ChartBarData(label = stringResource(R.string.chart_day_thu), value = 15f, detailText = stringResource(R.string.chart_detail_prompts, 15)),
        ChartBarData(label = stringResource(R.string.chart_day_fri), value = 3f, detailText = stringResource(R.string.chart_detail_prompts, 3)),
        ChartBarData(label = stringResource(R.string.chart_day_sat), value = 0f, detailText = stringResource(R.string.chart_detail_prompts, 0)),
        ChartBarData(label = stringResource(R.string.chart_day_sun), value = 7f, detailText = stringResource(R.string.chart_detail_prompts, 7)),
    )
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    UsageChart(
        bars = sampleChartBars,
        selectedIndex = selectedBarIndex,
        onBarTapped = { selectedBarIndex = it },
        modifier = Modifier.fillMaxWidth()
    )
}
