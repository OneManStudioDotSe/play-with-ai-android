package se.onemanstudio.playaroundwithai.core.ui.sofa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.core.ui.views.R
import kotlin.math.roundToInt

private val DragHandleWidth = 32.dp
private val DragHandleHeight = 4.dp
private val DragHandleCornerRadius = 2.dp
private const val MARKER_HEIGHT_FRACTION = 0.6f
private const val MARKER_OVERSHOOT_DP = 12f
private const val MARKER_SKEW_DP = 3f
private const val MARKER_ALPHA = 0.35f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    state: SettingsState,
    onDismiss: () -> Unit,
    onVehicleCountChange: (Int) -> Unit,
    onSearchRadiusChange: (Float) -> Unit,
    onContactClick: () -> Unit,
    usageBars: List<ChartBarData> = emptyList(),
    selectedDayIndex: Int? = null,
    onBarTapped: (Int) -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(Dimensions.paddingMedium),
        containerColor = Color.Transparent,
        dragHandle = null,
    ) {
        SettingsBottomSheetContent(
            state = state,
            onVehicleCountChange = onVehicleCountChange,
            onSearchRadiusChange = onSearchRadiusChange,
            onContactClick = onContactClick,
            usageBars = usageBars,
            selectedDayIndex = selectedDayIndex,
            onBarTapped = onBarTapped,
        )
    }
}

@Composable
private fun SettingsBottomSheetContent(
    state: SettingsState,
    onVehicleCountChange: (Int) -> Unit,
    onSearchRadiusChange: (Float) -> Unit,
    onContactClick: () -> Unit,
    usageBars: List<ChartBarData>,
    selectedDayIndex: Int?,
    onBarTapped: (Int) -> Unit,
) {
    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            // Custom drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.paddingMedium),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(DragHandleWidth)
                        .height(DragHandleHeight)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium),
                            shape = RoundedCornerShape(DragHandleCornerRadius),
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.paddingLarge)
                    .padding(top = Dimensions.paddingLarge, bottom = Dimensions.paddingExtraLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge),
            ) {
                // Title
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

                // Map Controls section
                MapControlsSection(
                    vehicleCount = state.vehicleCount,
                    searchRadiusKm = state.searchRadiusKm,
                    onVehicleCountChange = onVehicleCountChange,
                    onSearchRadiusChange = onSearchRadiusChange,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

                // Usage Chart section
                UsageSection(
                    usageBars = usageBars,
                    selectedDayIndex = selectedDayIndex,
                    onBarTapped = onBarTapped,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

                // About section
                AboutSection(
                    appVersion = state.appVersion,
                    onContactClick = onContactClick,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    var textWidth by remember { mutableFloatStateOf(0f) }

    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        onTextLayout = { result -> textWidth = result.size.width.toFloat() },
        modifier = modifier
            .drawBehind {
                if (textWidth <= 0f) return@drawBehind

                val markerHeight = size.height * MARKER_HEIGHT_FRACTION
                val overshoot = MARKER_OVERSHOOT_DP.dp.toPx()
                val markerWidth = textWidth + overshoot
                val verticalCenter = size.height / 2f
                val skew = MARKER_SKEW_DP.dp.toPx()

                val path = Path().apply {
                    moveTo(0f, verticalCenter - markerHeight / 2f + skew)
                    lineTo(markerWidth, verticalCenter - markerHeight / 2f)
                    lineTo(markerWidth, verticalCenter + markerHeight / 2f - skew)
                    lineTo(0f, verticalCenter + markerHeight / 2f)
                    close()
                }

                drawPath(
                    path = path,
                    color = lineColor.copy(alpha = MARKER_ALPHA),
                )
            },
    )
}

@Composable
private fun AboutSection(
    appVersion: String,
    onContactClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
        SectionHeader(
            text = stringResource(R.string.settings_about),
            lineColor = zestyLime,
        )

        Text(
            text = stringResource(R.string.settings_app_name),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = stringResource(R.string.settings_version, appVersion),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onContactClick() }
                .padding(vertical = Dimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = stringResource(R.string.settings_contact),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.settings_contact),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MapControlsSection(
    vehicleCount: Int,
    searchRadiusKm: Float,
    onVehicleCountChange: (Int) -> Unit,
    onSearchRadiusChange: (Float) -> Unit,
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.onSurface,
        activeTrackColor = MaterialTheme.colorScheme.onSurface,
        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.extraLow),
    )

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
        SectionHeader(
            text = stringResource(R.string.settings_map_controls),
            lineColor = electricBlue,
        )

        Text(
            text = stringResource(R.string.settings_vehicle_count, vehicleCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Slider(
            value = vehicleCount.toFloat(),
            onValueChange = { onVehicleCountChange(it.roundToInt()) },
            valueRange = SettingsState.MIN_VEHICLE_COUNT.toFloat()..SettingsState.MAX_VEHICLE_COUNT.toFloat(),
            steps = (SettingsState.MAX_VEHICLE_COUNT - SettingsState.MIN_VEHICLE_COUNT) / VEHICLE_STEP - 1,
            colors = sliderColors,
        )

        Text(
            text = stringResource(R.string.settings_search_radius, searchRadiusKm),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Slider(
            value = searchRadiusKm,
            onValueChange = { onSearchRadiusChange((it * RADIUS_DECIMAL_FACTOR).roundToInt() / RADIUS_DECIMAL_FACTOR) },
            valueRange = SettingsState.MIN_SEARCH_RADIUS_KM..SettingsState.MAX_SEARCH_RADIUS_KM,
            colors = sliderColors,
        )
    }
}

@Composable
private fun UsageSection(
    usageBars: List<ChartBarData>,
    selectedDayIndex: Int?,
    onBarTapped: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)) {
        SectionHeader(
            text = stringResource(R.string.settings_weekly_usage),
            lineColor = vividPink,
        )

        if (usageBars.size == WEEKDAY_COUNT) {
            UsageChart(
                bars = usageBars,
                selectedIndex = selectedDayIndex,
                onBarTapped = onBarTapped,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = stringResource(R.string.settings_no_usage_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val WEEKDAY_COUNT = 7

private const val VEHICLE_STEP = 10
private const val RADIUS_DECIMAL_FACTOR = 10f

private val sampleUsageBars = listOf(
    ChartBarData(label = "M", value = 120f, detailText = "120 tokens"),
    ChartBarData(label = "T", value = 85f, detailText = "85 tokens"),
    ChartBarData(label = "W", value = 200f, detailText = "200 tokens"),
    ChartBarData(label = "T", value = 0f, detailText = "0 tokens"),
    ChartBarData(label = "F", value = 150f, detailText = "150 tokens"),
    ChartBarData(label = "S", value = 45f, detailText = "45 tokens"),
    ChartBarData(label = "S", value = 90f, detailText = "90 tokens"),
)

@Preview(name = "Settings Content Light")
@Composable
private fun SettingsContentLightPreview() {
    SofaAiTheme {
        SettingsBottomSheetContent(
            state = SettingsState(appVersion = "1.0.0"),
            onVehicleCountChange = {},
            onSearchRadiusChange = {},
            onContactClick = {},
            usageBars = sampleUsageBars,
            selectedDayIndex = 2,
            onBarTapped = {},
        )
    }
}

@Preview(name = "Settings Content Dark")
@Composable
private fun SettingsContentDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        SettingsBottomSheetContent(
            state = SettingsState(appVersion = "1.0.0"),
            onVehicleCountChange = {},
            onSearchRadiusChange = {},
            onContactClick = {},
            usageBars = sampleUsageBars,
            selectedDayIndex = null,
            onBarTapped = {},
        )
    }
}

@Preview(name = "Section Headers")
@Composable
private fun SectionHeadersPreview() {
    SofaAiTheme {
        Column(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge),
        ) {
            SectionHeader(text = "Map Controls", lineColor = electricBlue)
            SectionHeader(text = "Weekly Usage", lineColor = vividPink)
            SectionHeader(text = "About", lineColor = zestyLime)
        }
    }
}

@Preview(name = "Usage Section — Empty")
@Composable
private fun UsageSectionEmptyPreview() {
    SofaAiTheme {
        UsageSection(
            usageBars = emptyList(),
            selectedDayIndex = null,
            onBarTapped = {},
        )
    }
}
