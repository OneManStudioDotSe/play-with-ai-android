@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.settings

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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.config.settings.AiPersona
import se.onemanstudio.playaroundwithai.core.ui.theme.cyberPurple
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.core.ui.theme.solarYellow
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.core.ui.sofa.ChartBarData
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.UsageChart
import kotlin.math.roundToInt

private val DragHandleWidth = 32.dp
private val DragHandleHeight = 4.dp
private val DragHandleCornerRadius = 2.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    screen: SettingsScreen,
    state: SettingsState,
    onDismiss: () -> Unit,
    onShowcaseClick: () -> Unit,
    onShowTokenUsageChange: (Boolean) -> Unit,
    onVehicleCountChange: (Int) -> Unit,
    onSearchRadiusChange: (Float) -> Unit,
    onWalkingSpeedChange: (Float) -> Unit,
    onTypewriterDelayChange: (Long) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onNetworkTimeoutChange: (Int) -> Unit,
    onTokenTrackingChange: (Boolean) -> Unit,
    onTripLengthChange: (Int) -> Unit,
    onFirebaseSyncChange: (Boolean) -> Unit,
    onImageQualityChange: (Int) -> Unit,
    onAgentMaxIterationsChange: (Int) -> Unit,
    onSuggestedPlacesCountChange: (Int) -> Unit,
    onMaxSelectablePointsChange: (Int) -> Unit,
    onGeminiTextModelChange: (String) -> Unit,
    onGeminiImageModelChange: (String) -> Unit,
    onAiPersonaChange: (AiPersona) -> Unit,
    onResetToDefaults: () -> Unit,
    onContactClick: () -> Unit,
    onLinkedInClick: () -> Unit = {},
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
            screen = screen,
            state = state,
            onShowcaseClick = onShowcaseClick,
            onShowTokenUsageChange = onShowTokenUsageChange,
            onVehicleCountChange = onVehicleCountChange,
            onSearchRadiusChange = onSearchRadiusChange,
            onWalkingSpeedChange = onWalkingSpeedChange,
            onTypewriterDelayChange = onTypewriterDelayChange,
            onHapticFeedbackChange = onHapticFeedbackChange,
            onNetworkTimeoutChange = onNetworkTimeoutChange,
            onTokenTrackingChange = onTokenTrackingChange,
            onTripLengthChange = onTripLengthChange,
            onFirebaseSyncChange = onFirebaseSyncChange,
            onImageQualityChange = onImageQualityChange,
            onAgentMaxIterationsChange = onAgentMaxIterationsChange,
            onSuggestedPlacesCountChange = onSuggestedPlacesCountChange,
            onMaxSelectablePointsChange = onMaxSelectablePointsChange,
            onGeminiTextModelChange = onGeminiTextModelChange,
            onGeminiImageModelChange = onGeminiImageModelChange,
            onAiPersonaChange = onAiPersonaChange,
            onResetToDefaults = onResetToDefaults,
            onContactClick = onContactClick,
            onLinkedInClick = onLinkedInClick,
            usageBars = usageBars,
            selectedDayIndex = selectedDayIndex,
            onBarTapped = onBarTapped,
        )
    }
}

@Composable
private fun SettingsBottomSheetContent(
    screen: SettingsScreen,
    state: SettingsState,
    onShowcaseClick: () -> Unit,
    onShowTokenUsageChange: (Boolean) -> Unit,
    onVehicleCountChange: (Int) -> Unit,
    onSearchRadiusChange: (Float) -> Unit,
    onWalkingSpeedChange: (Float) -> Unit,
    onTypewriterDelayChange: (Long) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onNetworkTimeoutChange: (Int) -> Unit,
    onTokenTrackingChange: (Boolean) -> Unit,
    onTripLengthChange: (Int) -> Unit,
    onFirebaseSyncChange: (Boolean) -> Unit,
    onImageQualityChange: (Int) -> Unit,
    onAgentMaxIterationsChange: (Int) -> Unit,
    onSuggestedPlacesCountChange: (Int) -> Unit,
    onMaxSelectablePointsChange: (Int) -> Unit,
    onGeminiTextModelChange: (String) -> Unit,
    onGeminiImageModelChange: (String) -> Unit,
    onAiPersonaChange: (AiPersona) -> Unit,
    onResetToDefaults: () -> Unit,
    onContactClick: () -> Unit,
    onLinkedInClick: () -> Unit,
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

                Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

                // Screen-specific section — shown at the top, varies per originating screen
                ScreenSpecificSection(
                    screen = screen,
                    state = state,
                    onAiPersonaChange = onAiPersonaChange,
                    onVehicleCountChange = onVehicleCountChange,
                    onSearchRadiusChange = onSearchRadiusChange,
                    onWalkingSpeedChange = onWalkingSpeedChange,
                    onMaxSelectablePointsChange = onMaxSelectablePointsChange,
                    onTripLengthChange = onTripLengthChange,
                    onAgentMaxIterationsChange = onAgentMaxIterationsChange,
                    onSuggestedPlacesCountChange = onSuggestedPlacesCountChange,
                    onImageQualityChange = onImageQualityChange,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

                // Common sections — always shown, at the bottom regardless of screen
                GeneralSection(
                    showTokenUsage = state.showTokenUsage,
                    typewriterDelayMs = state.typewriterDelayMs,
                    hapticFeedbackEnabled = state.hapticFeedbackEnabled,
                    networkTimeoutSeconds = state.networkTimeoutSeconds,
                    tokenTrackingEnabled = state.tokenTrackingEnabled,
                    firebaseSyncEnabled = state.firebaseSyncEnabled,
                    onShowTokenUsageChange = onShowTokenUsageChange,
                    onTypewriterDelayChange = onTypewriterDelayChange,
                    onHapticFeedbackChange = onHapticFeedbackChange,
                    onNetworkTimeoutChange = onNetworkTimeoutChange,
                    onTokenTrackingChange = onTokenTrackingChange,
                    onFirebaseSyncChange = onFirebaseSyncChange,
                    onShowcaseClick = onShowcaseClick,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

                AiModelsSection(
                    geminiTextModel = state.geminiTextModel,
                    geminiImageModel = state.geminiImageModel,
                    onGeminiTextModelChange = onGeminiTextModelChange,
                    onGeminiImageModelChange = onGeminiImageModelChange,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

                UsageSection(
                    usageBars = usageBars,
                    selectedDayIndex = selectedDayIndex,
                    onBarTapped = onBarTapped,
                )

                Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

                AboutSection(
                    appVersion = state.appVersion,
                    onResetToDefaults = onResetToDefaults,
                    onContactClick = onContactClick,
                    onLinkedInClick = onLinkedInClick,
                )
            }
        }
    }
}

@Composable
private fun ScreenSpecificSection(
    screen: SettingsScreen,
    state: SettingsState,
    onAiPersonaChange: (AiPersona) -> Unit,
    onVehicleCountChange: (Int) -> Unit,
    onSearchRadiusChange: (Float) -> Unit,
    onWalkingSpeedChange: (Float) -> Unit,
    onMaxSelectablePointsChange: (Int) -> Unit,
    onTripLengthChange: (Int) -> Unit,
    onAgentMaxIterationsChange: (Int) -> Unit,
    onSuggestedPlacesCountChange: (Int) -> Unit,
    onImageQualityChange: (Int) -> Unit,
) {
    when (screen) {
        SettingsScreen.CHAT -> AiPersonaSection(
            aiPersona = state.aiPersona,
            onAiPersonaChange = onAiPersonaChange,
        )

        SettingsScreen.EXPLORE -> ExploreControlsSection(
            vehicleCount = state.vehicleCount,
            searchRadiusKm = state.searchRadiusKm,
            walkingSpeedKmh = state.walkingSpeedKmh,
            maxSelectablePoints = state.maxSelectablePoints,
            onVehicleCountChange = onVehicleCountChange,
            onSearchRadiusChange = onSearchRadiusChange,
            onWalkingSpeedChange = onWalkingSpeedChange,
            onMaxSelectablePointsChange = onMaxSelectablePointsChange,
        )

        SettingsScreen.DREAM -> ImageGenerationSection(
            imageQualityJpeg = state.imageQualityJpeg,
            onImageQualityChange = onImageQualityChange,
        )

        SettingsScreen.PLAN -> PlanControlsSection(
            walkingSpeedKmh = state.walkingSpeedKmh,
            tripLengthMinStops = state.tripLengthMinStops,
            agentMaxIterations = state.agentMaxIterations,
            suggestedPlacesCount = state.suggestedPlacesCount,
            onWalkingSpeedChange = onWalkingSpeedChange,
            onTripLengthChange = onTripLengthChange,
            onAgentMaxIterationsChange = onAgentMaxIterationsChange,
            onSuggestedPlacesCountChange = onSuggestedPlacesCountChange,
        )
    }
}

@Composable
private fun SectionHeader(
    text: String,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    MarkerText(text = text, lineColor = lineColor, modifier = modifier)
}

@Composable
private fun settingsSliderColors(): SliderColors = SliderDefaults.colors(
    thumbColor = MaterialTheme.colorScheme.onSurface,
    activeTrackColor = MaterialTheme.colorScheme.onSurface,
    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.extraLow),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneralSection(
    showTokenUsage: Boolean,
    typewriterDelayMs: Long,
    hapticFeedbackEnabled: Boolean,
    networkTimeoutSeconds: Int,
    tokenTrackingEnabled: Boolean,
    firebaseSyncEnabled: Boolean,
    onShowTokenUsageChange: (Boolean) -> Unit,
    onTypewriterDelayChange: (Long) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onNetworkTimeoutChange: (Int) -> Unit,
    onTokenTrackingChange: (Boolean) -> Unit,
    onFirebaseSyncChange: (Boolean) -> Unit,
    onShowcaseClick: () -> Unit,
) {
    val speedOptions = listOf(
        Triple(SettingsState.TYPEWRITER_DELAY_INSTANT,
            stringResource(R.string.settings_typewriter_speed_instant),
            stringResource(R.string.settings_typewriter_speed_instant_value)),
        Triple(SettingsState.TYPEWRITER_DELAY_FAST,
            stringResource(R.string.settings_typewriter_speed_fast),
            stringResource(R.string.settings_typewriter_speed_fast_value)),
        Triple(SettingsState.TYPEWRITER_DELAY_NORMAL,
            stringResource(R.string.settings_typewriter_speed_normal),
            stringResource(R.string.settings_typewriter_speed_normal_value)),
        Triple(SettingsState.TYPEWRITER_DELAY_SLOW,
            stringResource(R.string.settings_typewriter_speed_slow),
            stringResource(R.string.settings_typewriter_speed_slow_value)),
    )

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        SectionHeader(
            text = stringResource(R.string.settings_general),
            lineColor = electricBlue,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_show_token_usage),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
                checked = showTokenUsage,
                onCheckedChange = onShowTokenUsageChange,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_token_tracking),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
                checked = tokenTrackingEnabled,
                onCheckedChange = onTokenTrackingChange,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_haptic_feedback),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
                checked = hapticFeedbackEnabled,
                onCheckedChange = onHapticFeedbackChange,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_firebase_sync),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
                checked = firebaseSyncEnabled,
                onCheckedChange = onFirebaseSyncChange,
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

        Text(
            text = stringResource(R.string.settings_typewriter_speed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            speedOptions.forEachIndexed { index, (delay, label, sublabel) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = speedOptions.size),
                    selected = typewriterDelayMs == delay,
                    onClick = { onTypewriterDelayChange(delay) },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = label, style = MaterialTheme.typography.labelSmall)
                        Text(text = sublabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.settings_network_timeout, networkTimeoutSeconds),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Slider(
            value = networkTimeoutSeconds.toFloat(),
            onValueChange = { onNetworkTimeoutChange(it.roundToInt()) },
            valueRange = SettingsState.MIN_NETWORK_TIMEOUT_SECONDS.toFloat()..SettingsState.MAX_NETWORK_TIMEOUT_SECONDS.toFloat(),
            steps = (SettingsState.MAX_NETWORK_TIMEOUT_SECONDS - SettingsState.MIN_NETWORK_TIMEOUT_SECONDS) /
                SettingsState.NETWORK_TIMEOUT_STEP_SECONDS - 1,
            colors = settingsSliderColors(),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowcaseClick() }
                .padding(vertical = Dimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = stringResource(R.string.settings_showcase),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.settings_showcase),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun AboutSection(
    appVersion: String,
    onResetToDefaults: () -> Unit,
    onContactClick: () -> Unit,
    onLinkedInClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        SectionHeader(
            text = stringResource(R.string.settings_about),
            lineColor = zestyLime,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
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
        }

        Text(
            text = stringResource(R.string.settings_app_description),
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLinkedInClick() }
                .padding(vertical = Dimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.settings_linkedin),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.settings_linkedin),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

        NeoBrutalButton(
            text = stringResource(R.string.settings_reset_defaults),
            onClick = onResetToDefaults,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreControlsSection(
    vehicleCount: Int,
    searchRadiusKm: Float,
    walkingSpeedKmh: Float,
    maxSelectablePoints: Int,
    onVehicleCountChange: (Int) -> Unit,
    onSearchRadiusChange: (Float) -> Unit,
    onWalkingSpeedChange: (Float) -> Unit,
    onMaxSelectablePointsChange: (Int) -> Unit,
) {
    val sliderColors = settingsSliderColors()

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        SectionHeader(
            text = stringResource(R.string.settings_map_controls),
            lineColor = vividPink,
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

        WalkingSpeedSelector(
            walkingSpeedKmh = walkingSpeedKmh,
            onWalkingSpeedChange = onWalkingSpeedChange,
        )

        Text(
            text = stringResource(R.string.settings_max_route_points, maxSelectablePoints),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Slider(
            value = maxSelectablePoints.toFloat(),
            onValueChange = { onMaxSelectablePointsChange(it.roundToInt()) },
            valueRange = SettingsState.MIN_MAX_SELECTABLE_POINTS.toFloat()..SettingsState.MAX_MAX_SELECTABLE_POINTS.toFloat(),
            steps = SettingsState.MAX_MAX_SELECTABLE_POINTS - SettingsState.MIN_MAX_SELECTABLE_POINTS - 1,
            colors = sliderColors,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanControlsSection(
    walkingSpeedKmh: Float,
    tripLengthMinStops: Int,
    agentMaxIterations: Int,
    suggestedPlacesCount: Int,
    onWalkingSpeedChange: (Float) -> Unit,
    onTripLengthChange: (Int) -> Unit,
    onAgentMaxIterationsChange: (Int) -> Unit,
    onSuggestedPlacesCountChange: (Int) -> Unit,
) {
    val sliderColors = settingsSliderColors()

    val tripLengthOptions = listOf(
        Triple(SettingsState.TRIP_LENGTH_QUICK_MIN,
            stringResource(R.string.settings_trip_length_quick),
            stringResource(R.string.settings_trip_length_quick_value)),
        Triple(SettingsState.TRIP_LENGTH_STANDARD_MIN,
            stringResource(R.string.settings_trip_length_standard),
            stringResource(R.string.settings_trip_length_standard_value)),
        Triple(SettingsState.TRIP_LENGTH_EXTENDED_MIN,
            stringResource(R.string.settings_trip_length_extended),
            stringResource(R.string.settings_trip_length_extended_value)),
    )

    val agentIterationOptions = listOf(
        Triple(SettingsState.AGENT_ITERATIONS_QUICK,
            stringResource(R.string.settings_agent_iterations_quick),
            stringResource(R.string.settings_agent_iterations_quick_value)),
        Triple(SettingsState.AGENT_ITERATIONS_STANDARD,
            stringResource(R.string.settings_agent_iterations_standard),
            stringResource(R.string.settings_agent_iterations_standard_value)),
        Triple(SettingsState.AGENT_ITERATIONS_THOROUGH,
            stringResource(R.string.settings_agent_iterations_thorough),
            stringResource(R.string.settings_agent_iterations_thorough_value)),
    )

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        SectionHeader(
            text = stringResource(R.string.settings_trip_planner),
            lineColor = vividPink,
        )

        WalkingSpeedSelector(
            walkingSpeedKmh = walkingSpeedKmh,
            onWalkingSpeedChange = onWalkingSpeedChange,
        )

        Text(
            text = stringResource(R.string.settings_trip_length),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            tripLengthOptions.forEachIndexed { index, (minStops, label, sublabel) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tripLengthOptions.size),
                    selected = tripLengthMinStops == minStops,
                    onClick = { onTripLengthChange(minStops) },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = label, style = MaterialTheme.typography.labelSmall)
                        Text(text = sublabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.settings_agent_iterations),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            agentIterationOptions.forEachIndexed { index, (iterations, label, sublabel) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = agentIterationOptions.size),
                    selected = agentMaxIterations == iterations,
                    onClick = { onAgentMaxIterationsChange(iterations) },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = label, style = MaterialTheme.typography.labelSmall)
                        Text(text = sublabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.settings_suggested_places_count, suggestedPlacesCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Slider(
            value = suggestedPlacesCount.toFloat(),
            onValueChange = { onSuggestedPlacesCountChange(it.roundToInt()) },
            valueRange = SettingsState.MIN_SUGGESTED_PLACES_COUNT.toFloat()..SettingsState.MAX_SUGGESTED_PLACES_COUNT.toFloat(),
            steps = (SettingsState.MAX_SUGGESTED_PLACES_COUNT - SettingsState.MIN_SUGGESTED_PLACES_COUNT) /
                SettingsState.SUGGESTED_PLACES_COUNT_STEP - 1,
            colors = sliderColors,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalkingSpeedSelector(
    walkingSpeedKmh: Float,
    onWalkingSpeedChange: (Float) -> Unit,
) {
    val speedOptions = listOf(
        Triple(SettingsState.WALKING_SPEED_SLOW,
            stringResource(R.string.settings_walking_speed_slow),
            stringResource(R.string.settings_walking_speed_slow_value)),
        Triple(SettingsState.WALKING_SPEED_NORMAL,
            stringResource(R.string.settings_walking_speed_normal),
            stringResource(R.string.settings_walking_speed_normal_value)),
        Triple(SettingsState.WALKING_SPEED_FAST,
            stringResource(R.string.settings_walking_speed_fast),
            stringResource(R.string.settings_walking_speed_fast_value)),
    )

    Text(
        text = stringResource(R.string.settings_walking_speed),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        speedOptions.forEachIndexed { index, (speed, label, sublabel) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = speedOptions.size),
                selected = walkingSpeedKmh == speed,
                onClick = { onWalkingSpeedChange(speed) },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = label, style = MaterialTheme.typography.labelSmall)
                    Text(text = sublabel, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageGenerationSection(
    imageQualityJpeg: Int,
    onImageQualityChange: (Int) -> Unit,
) {
    val imageQualityOptions = listOf(
        Triple(SettingsState.IMAGE_QUALITY_LOW,
            stringResource(R.string.settings_image_quality_low),
            stringResource(R.string.settings_image_quality_low_value)),
        Triple(SettingsState.IMAGE_QUALITY_MEDIUM,
            stringResource(R.string.settings_image_quality_medium),
            stringResource(R.string.settings_image_quality_medium_value)),
        Triple(SettingsState.IMAGE_QUALITY_HIGH,
            stringResource(R.string.settings_image_quality_high),
            stringResource(R.string.settings_image_quality_high_value)),
    )

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        SectionHeader(
            text = stringResource(R.string.settings_image_generation),
            lineColor = vividPink,
        )

        Text(
            text = stringResource(R.string.settings_image_quality),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            imageQualityOptions.forEachIndexed { index, (quality, label, sublabel) ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = imageQualityOptions.size),
                    selected = imageQualityJpeg == quality,
                    onClick = { onImageQualityChange(quality) },
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = label, style = MaterialTheme.typography.labelSmall)
                        Text(text = sublabel, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiPersonaSection(
    aiPersona: AiPersona,
    onAiPersonaChange: (AiPersona) -> Unit,
) {
    val personaOptions = listOf(
        Triple(AiPersona.AI_OVERLORD,
            stringResource(R.string.settings_persona_overlord_name),
            stringResource(R.string.settings_persona_overlord_desc)),
        Triple(AiPersona.FLATTERER,
            stringResource(R.string.settings_persona_flatterer_name),
            stringResource(R.string.settings_persona_flatterer_desc)),
        Triple(AiPersona.GRUMPY_OLD_MAN,
            stringResource(R.string.settings_persona_grumpy_name),
            stringResource(R.string.settings_persona_grumpy_desc)),
        Triple(AiPersona.KAREN,
            stringResource(R.string.settings_persona_karen_name),
            stringResource(R.string.settings_persona_karen_desc)),
        Triple(AiPersona.CAVEMAN,
            stringResource(R.string.settings_persona_caveman_name),
            stringResource(R.string.settings_persona_caveman_desc)),
    )

    var expanded by remember { mutableStateOf(false) }
    val currentName = personaOptions.find { it.first == aiPersona }?.second ?: aiPersona.name

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        SectionHeader(
            text = stringResource(R.string.settings_ai_persona),
            lineColor = cyberPurple,
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = currentName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                personaOptions.forEach { (persona, name, desc) ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            onAiPersonaChange(persona)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiModelsSection(
    geminiTextModel: String,
    geminiImageModel: String,
    onGeminiTextModelChange: (String) -> Unit,
    onGeminiImageModelChange: (String) -> Unit,
) {
    val textModelOptions = listOf(
        Triple(SettingsState.GEMINI_TEXT_MODEL_DEFAULT,
            stringResource(R.string.settings_gemini_text_flash3_preview_name),
            stringResource(R.string.settings_gemini_text_flash3_preview_desc)),
        Triple(SettingsState.GEMINI_TEXT_MODEL_FLASH_25,
            stringResource(R.string.settings_gemini_text_flash25_name),
            stringResource(R.string.settings_gemini_text_flash25_desc)),
        Triple(SettingsState.GEMINI_TEXT_MODEL_FLASH_25_LITE,
            stringResource(R.string.settings_gemini_text_flash25_lite_name),
            stringResource(R.string.settings_gemini_text_flash25_lite_desc)),
        Triple(SettingsState.GEMINI_TEXT_MODEL_PRO_25,
            stringResource(R.string.settings_gemini_text_pro25_name),
            stringResource(R.string.settings_gemini_text_pro25_desc)),
        Triple(SettingsState.GEMINI_TEXT_MODEL_PRO_31_PREVIEW,
            stringResource(R.string.settings_gemini_text_pro31_preview_name),
            stringResource(R.string.settings_gemini_text_pro31_preview_desc)),
    )

    val imageModelOptions = listOf(
        Triple(SettingsState.GEMINI_IMAGE_MODEL_DEFAULT,
            stringResource(R.string.settings_gemini_image_flash25_name),
            stringResource(R.string.settings_gemini_image_flash25_desc)),
        Triple(SettingsState.GEMINI_IMAGE_MODEL_FLASH_31_PREVIEW,
            stringResource(R.string.settings_gemini_image_flash31_preview_name),
            stringResource(R.string.settings_gemini_image_flash31_preview_desc)),
        Triple(SettingsState.GEMINI_IMAGE_MODEL_PRO_3_PREVIEW,
            stringResource(R.string.settings_gemini_image_pro3_preview_name),
            stringResource(R.string.settings_gemini_image_pro3_preview_desc)),
    )

    var textModelExpanded by remember { mutableStateOf(false) }
    var imageModelExpanded by remember { mutableStateOf(false) }

    val currentTextModelName = textModelOptions.find { it.first == geminiTextModel }?.second ?: geminiTextModel
    val currentImageModelName = imageModelOptions.find { it.first == geminiImageModel }?.second ?: geminiImageModel

    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        SectionHeader(
            text = stringResource(R.string.settings_ai_models),
            lineColor = solarYellow,
        )

        Text(
            text = stringResource(R.string.settings_gemini_text_model),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        ExposedDropdownMenuBox(
            expanded = textModelExpanded,
            onExpandedChange = { textModelExpanded = it },
        ) {
            OutlinedTextField(
                value = currentTextModelName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = textModelExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            )
            ExposedDropdownMenu(
                expanded = textModelExpanded,
                onDismissRequest = { textModelExpanded = false },
            ) {
                textModelOptions.forEach { (id, name, desc) ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            onGeminiTextModelChange(id)
                            textModelExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.settings_gemini_image_model),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        ExposedDropdownMenuBox(
            expanded = imageModelExpanded,
            onExpandedChange = { imageModelExpanded = it },
        ) {
            OutlinedTextField(
                value = currentImageModelName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = imageModelExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            )
            ExposedDropdownMenu(
                expanded = imageModelExpanded,
                onDismissRequest = { imageModelExpanded = false },
            ) {
                imageModelOptions.forEach { (id, name, desc) ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            onGeminiImageModelChange(id)
                            imageModelExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageSection(
    usageBars: List<ChartBarData>,
    selectedDayIndex: Int?,
    onBarTapped: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)) {
        SectionHeader(
            text = stringResource(R.string.settings_weekly_usage),
            lineColor = energeticOrange,
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

@Preview(name = "Settings Content — Explore")
@Composable
private fun SettingsContentExplorePreview() {
    SofaAiTheme {
        Surface {
            SettingsBottomSheetContent(
                screen = SettingsScreen.EXPLORE,
                state = SettingsState(appVersion = "1.0.0"),
                onShowcaseClick = {},
                onShowTokenUsageChange = {},
                onVehicleCountChange = {},
                onSearchRadiusChange = {},
                onWalkingSpeedChange = {},
                onTypewriterDelayChange = {},
                onHapticFeedbackChange = {},
                onNetworkTimeoutChange = {},
                onTokenTrackingChange = {},
                onTripLengthChange = {},
                onFirebaseSyncChange = {},
                onImageQualityChange = {},
                onAgentMaxIterationsChange = {},
                onSuggestedPlacesCountChange = {},
                onMaxSelectablePointsChange = {},
                onGeminiTextModelChange = {},
                onGeminiImageModelChange = {},
                onAiPersonaChange = {},
                onResetToDefaults = {},
                onContactClick = {},
                onLinkedInClick = {},
                usageBars = sampleUsageBars,
                selectedDayIndex = 2,
                onBarTapped = {},
            )
        }
    }
}

@Preview(name = "Settings Content — Plan (Dark)")
@Composable
private fun SettingsContentPlanDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            SettingsBottomSheetContent(
                screen = SettingsScreen.PLAN,
                state = SettingsState(appVersion = "1.0.0"),
                onShowcaseClick = {},
                onShowTokenUsageChange = {},
                onVehicleCountChange = {},
                onSearchRadiusChange = {},
                onWalkingSpeedChange = {},
                onTypewriterDelayChange = {},
                onHapticFeedbackChange = {},
                onNetworkTimeoutChange = {},
                onTokenTrackingChange = {},
                onTripLengthChange = {},
                onFirebaseSyncChange = {},
                onImageQualityChange = {},
                onAgentMaxIterationsChange = {},
                onSuggestedPlacesCountChange = {},
                onMaxSelectablePointsChange = {},
                onGeminiTextModelChange = {},
                onGeminiImageModelChange = {},
                onAiPersonaChange = {},
                onResetToDefaults = {},
                onContactClick = {},
                onLinkedInClick = {},
                usageBars = sampleUsageBars,
                selectedDayIndex = null,
                onBarTapped = {},
            )
        }
    }
}

@Preview(name = "Section Headers")
@Composable
private fun SectionHeadersPreview() {
    SofaAiTheme {
        Surface {
            Column(
                modifier = Modifier.padding(Dimensions.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge),
            ) {
                SectionHeader(text = "General", lineColor = electricBlue)
                SectionHeader(text = "Map Controls", lineColor = vividPink)
                SectionHeader(text = "Weekly Usage", lineColor = energeticOrange)
                SectionHeader(text = "About", lineColor = zestyLime)
            }
        }
    }
}

@Preview(name = "Usage Section — Empty")
@Composable
private fun UsageSectionEmptyPreview() {
    SofaAiTheme {
        Surface {
            UsageSection(
                usageBars = emptyList(),
                selectedDayIndex = null,
                onBarTapped = {},
            )
        }
    }
}
