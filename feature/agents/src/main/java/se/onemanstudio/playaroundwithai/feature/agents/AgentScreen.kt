@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.agents

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.collections.immutable.PersistentList
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTextField
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.feature.agents.states.AgentError
import se.onemanstudio.playaroundwithai.feature.agents.states.AgentStepUi
import se.onemanstudio.playaroundwithai.feature.agents.states.AgentUiState
import se.onemanstudio.playaroundwithai.feature.agents.states.StepIcon
import se.onemanstudio.playaroundwithai.feature.agents.states.TripPlanUi
import se.onemanstudio.playaroundwithai.feature.agents.states.TripStopUi

private const val MAP_HEIGHT = 280
private const val MAP_ZOOM = 13f
private const val POLYLINE_WIDTH = 5f
private const val PULSE_ALPHA_MIN = 0.3f
private const val PULSE_ALPHA_MAX = 1.0f
private const val PULSE_DURATION_MS = 800

@Composable
fun AgentScreen(viewModel: AgentViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    LaunchedEffect(uiState) {
        when (uiState) {
            is AgentUiState.Result -> view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            is AgentUiState.Error -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            else -> {}
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoBrutalTopAppBar(title = stringResource(R.string.agent_title))
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = uiState) {
                is AgentUiState.Initial -> InitialState(
                    textState = textState,
                    onTextChanged = { textState = it },
                    onPlanClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.planTrip(textState.text)
                        keyboardController?.hide()
                    },
                )

                is AgentUiState.Running -> RunningState(state = state)

                is AgentUiState.Result -> ResultState(
                    state = state,
                    onNewPlan = {
                        textState = TextFieldValue("")
                        viewModel.resetToInitial()
                    },
                )

                is AgentUiState.Error -> ErrorState(
                    state = state,
                    onClearError = { viewModel.resetToInitial() },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InitialState(
    textState: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onPlanClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NeoBrutalTextField(
            value = textState,
            onValueChange = onTextChanged,
            placeholder = stringResource(R.string.agent_input_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

        NeoBrutalButton(
            text = stringResource(R.string.agent_plan_button),
            enabled = textState.text.isNotBlank(),
            backgroundColor = MaterialTheme.colorScheme.primary,
            onClick = onPlanClick,
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

        Text(
            text = "Try one of these:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimensions.paddingMedium),
        )

        val coffeeText = stringResource(R.string.agent_example_coffee)
        val museumsText = stringResource(R.string.agent_example_museums)
        val parksText = stringResource(R.string.agent_example_parks)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium),
        ) {
            NeoBrutalChip(
                text = coffeeText,
                onClick = { onTextChanged(TextFieldValue(coffeeText)) },
            )
            NeoBrutalChip(
                text = museumsText,
                onClick = { onTextChanged(TextFieldValue(museumsText)) },
            )
            NeoBrutalChip(
                text = parksText,
                onClick = { onTextChanged(TextFieldValue(parksText)) },
            )
        }
    }
}

@Composable
private fun RunningState(state: AgentUiState.Running) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimensions.iconSizeXXLarge),
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

        Text(
            text = "Planning your trip...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

        state.steps.forEachIndexed { index, step ->
            val isLast = index == state.steps.size - 1

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                StepRow(step = step, isPulsing = isLast)
            }

            if (!isLast) {
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            }
        }
    }
}

@Composable
private fun StepRow(step: AgentStepUi, isPulsing: Boolean) {
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = stepIcon(step.icon),
            contentDescription = null,
            tint = stepIconColor(step.icon),
            modifier = Modifier.size(Dimensions.iconSizeLarge),
        )
        Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
        Text(
            text = step.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
private fun ResultState(
    state: AgentUiState.Result,
    onNewPlan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        if (state.plan.stops.isNotEmpty()) {
            TripMap(stops = state.plan.stops)
        }

        Column(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                    Text(
                        text = state.plan.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            Text(
                text = stringResource(R.string.agent_your_itinerary),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

            state.plan.stops.forEach { stop ->
                StopCard(stop = stop)
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.agent_walking_distance, state.plan.totalDistanceKm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.agent_walking_time, state.plan.totalWalkingMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            NeoBrutalButton(
                text = stringResource(R.string.agent_new_plan_button),
                backgroundColor = MaterialTheme.colorScheme.secondary,
                onClick = onNewPlan,
            )
        }
    }
}

@Composable
private fun TripMap(stops: PersistentList<TripStopUi>) {
    val firstStop = stops.first()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(firstStop.latitude, firstStop.longitude),
            MAP_ZOOM,
        )
    }

    val routePoints = stops.map { LatLng(it.latitude, it.longitude) }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(MAP_HEIGHT.dp),
        cameraPositionState = cameraPositionState,
    ) {
        stops.forEach { stop ->
            Marker(
                state = rememberUpdatedMarkerState(position = LatLng(stop.latitude, stop.longitude)),
                title = "${stop.orderIndex + 1}. ${stop.name}",
                snippet = stop.category,
            )
        }

        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints,
                color = androidx.compose.ui.graphics.Color(0xFF1976D2),
                width = POLYLINE_WIDTH,
            )
        }
    }
}

@Composable
private fun StopCard(stop: TripStopUi) {
    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(Dimensions.paddingLarge)) {
            Text(
                text = "${stop.orderIndex + 1}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (stop.category.isNotBlank()) {
                    Text(
                        text = stop.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = se.onemanstudio.playaroundwithai.core.ui.theme.Alphas.medium),
                    )
                }
                if (stop.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                    Text(
                        text = stop.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    state: AgentUiState.Error,
    onClearError: () -> Unit,
) {
    val (errorMsg, errorIcon) = getErrorMessageAndIcon(state.error)

    NeoBrutalCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.paddingLarge),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(Dimensions.paddingLarge),
        ) {
            Icon(
                imageVector = errorIcon,
                contentDescription = stringResource(R.string.agent_label_error_icon),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimensions.iconSizeXLarge),
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.agent_oops),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (state.error !is AgentError.ApiKeyMissing) {
                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                NeoBrutalIconButton(
                    onClick = onClearError,
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.agent_label_dismiss_error),
                    backgroundColor = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun getErrorMessageAndIcon(error: AgentError): Pair<String, ImageVector> {
    return when (error) {
        is AgentError.ApiKeyMissing -> stringResource(R.string.agent_error_api_key_missing) to Icons.Rounded.VpnKey
        is AgentError.NetworkMissing -> stringResource(R.string.agent_error_network) to Icons.Rounded.WifiOff
        is AgentError.Unknown -> (error.message ?: stringResource(R.string.agent_error_unknown)) to Icons.Rounded.Warning
    }
}
