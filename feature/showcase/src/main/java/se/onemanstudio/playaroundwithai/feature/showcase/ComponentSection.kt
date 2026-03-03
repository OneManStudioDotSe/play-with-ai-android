@file:Suppress("TooManyFunctions", "MagicNumber")

package se.onemanstudio.playaroundwithai.feature.showcase

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

private val sampleChartBars = listOf(
    ChartBarData(label = "Mon", value = 5f, detailText = "5 prompts"),
    ChartBarData(label = "Tue", value = 12f, detailText = "12 prompts"),
    ChartBarData(label = "Wed", value = 8f, detailText = "8 prompts"),
    ChartBarData(label = "Thu", value = 15f, detailText = "15 prompts"),
    ChartBarData(label = "Fri", value = 3f, detailText = "3 prompts"),
    ChartBarData(label = "Sat", value = 0f, detailText = "0 prompts"),
    ChartBarData(label = "Sun", value = 7f, detailText = "7 prompts"),
)

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
            iconContentDescription = "Send",
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
            contentDescription = "Star",
            backgroundColor = electricBlue,
            onClick = {}
        )
        NeoBrutalIconButton(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favorite",
            backgroundColor = vividPink,
            onClick = {}
        )
        NeoBrutalIconButton(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = "ThumbUp",
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
            contentDescription = "Star",
            onClick = {}
        )
        NeoBrutalIconButtonSmall(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favorite",
            onClick = {}
        )
        NeoBrutalIconButtonSmall(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
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
                    contentDescription = "Settings",
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
        MarkerText(text = "Electric Blue", lineColor = electricBlue)
        MarkerText(text = "Vivid Pink", lineColor = vividPink)
        MarkerText(text = "Zesty Lime", lineColor = zestyLime)
        MarkerText(text = "Solar Yellow", lineColor = solarYellow)
        MarkerText(text = "Energetic Orange", lineColor = energeticOrange)
    }
}

@Composable
private fun UsageChartDemo() {
    ComponentLabel("UsageChart")
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    UsageChart(
        bars = sampleChartBars,
        selectedIndex = selectedBarIndex,
        onBarTapped = { selectedBarIndex = it },
        modifier = Modifier.fillMaxWidth()
    )
}
