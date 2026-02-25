@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.plan

import android.Manifest
import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalIconButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTextField
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalTopAppBar
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanError
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanStepUi
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState
import se.onemanstudio.playaroundwithai.feature.plan.states.StepIcon
import se.onemanstudio.playaroundwithai.feature.plan.states.TripPlanUi
import se.onemanstudio.playaroundwithai.feature.plan.states.TripStopUi

private const val DEFAULT_LAT = 59.3293
private const val DEFAULT_LNG = 18.0686
private const val MAP_HEIGHT_MIN = 180
private const val MAP_HEIGHT_MAX = 280
private const val MAP_ZOOM = 13f
private const val POLYLINE_WIDTH = 5f
private const val PULSE_ALPHA_MIN = 0.3f
private const val PULSE_ALPHA_MAX = 1.0f
private const val PULSE_DURATION_MS = 800
private const val BOUNCE_DURATION_MS = 400
private const val BOUNCE_STAGGER_MS = 100
private const val BOUNCE_DOT_COUNT = 3
private val BOUNCE_AMPLITUDE = 4.dp

@SuppressLint("MissingPermission")
@Composable
fun PlanScreen(
    viewModel: PlanViewModel = hiltViewModel(),
    settingsContent: @Composable (() -> Unit) -> Unit = { _ -> },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var showSettings by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    var userLatitude by remember { mutableStateOf(DEFAULT_LAT) }
    var userLongitude by remember { mutableStateOf(DEFAULT_LNG) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLatitude = it.latitude
                    userLongitude = it.longitude
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is PlanUiState.Result -> view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            is PlanUiState.Error -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            else -> {}
        }
    }

    if (showSettings) {
        settingsContent { showSettings = false }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NeoBrutalTopAppBar(
                title = stringResource(R.string.plan_title),
                actions = {
                    NeoBrutalIconButton(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(
                            se.onemanstudio.playaroundwithai.core.ui.views.R.string.settings_icon_description
                        ),
                        onClick = { showSettings = true },
                    )
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = uiState) {
                is PlanUiState.Initial -> InitialState(
                    textState = textState,
                    onTextChanged = { textState = it },
                    onPlanClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        viewModel.planTrip(textState.text, userLatitude, userLongitude)
                        keyboardController?.hide()
                    },
                )

                is PlanUiState.Running -> RunningState(state = state)

                is PlanUiState.Result -> ResultState(
                    state = state,
                    onNewPlan = {
                        textState = TextFieldValue("")
                        viewModel.resetToInitial()
                    },
                )

                is PlanUiState.Error -> ErrorState(
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
            placeholder = stringResource(R.string.plan_input_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

        NeoBrutalButton(
            text = stringResource(R.string.plan_plan_button),
            enabled = textState.text.isNotBlank(),
            backgroundColor = MaterialTheme.colorScheme.primary,
            onClick = onPlanClick,
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

        Text(
            text = stringResource(R.string.plan_try_suggestions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimensions.paddingMedium),
        )

        val coffeeText = stringResource(R.string.plan_example_coffee)
        val museumsText = stringResource(R.string.plan_example_museums)
        val parksText = stringResource(R.string.plan_example_parks)

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
private fun RunningState(state: PlanUiState.Running) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = stringResource(R.string.plan_icon_planning),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimensions.iconSizeXXLarge),
        )

        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

        Text(
            text = stringResource(R.string.plan_planning_trip),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = stepIcon(step.icon),
            contentDescription = stepIconContentDescription(step.icon),
            tint = stepIconColor(step.icon),
            modifier = Modifier.size(Dimensions.iconSizeLarge),
        )
        Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
        Text(
            text = displayLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (showBouncingDots) {
            BouncingDots()
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
private fun stepIconContentDescription(icon: StepIcon) = when (icon) {
    StepIcon.THINKING -> stringResource(R.string.plan_icon_step_thinking)
    StepIcon.TOOL_CALL -> stringResource(R.string.plan_icon_step_tool_call)
    StepIcon.TOOL_RESULT -> stringResource(R.string.plan_icon_step_tool_result)
}

@Composable
private fun ResultState(
    state: PlanUiState.Result,
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
                text = stringResource(R.string.plan_your_itinerary),
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
                    text = stringResource(R.string.plan_walking_distance, state.plan.totalDistanceKm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.plan_walking_time, state.plan.totalWalkingMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            NeoBrutalButton(
                text = stringResource(R.string.plan_new_plan_button),
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
            .heightIn(min = MAP_HEIGHT_MIN.dp, max = MAP_HEIGHT_MAX.dp),
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
                color = MaterialTheme.colorScheme.primary,
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
    state: PlanUiState.Error,
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
                contentDescription = stringResource(R.string.plan_label_error_icon),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimensions.iconSizeXLarge),
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            Text(
                text = stringResource(R.string.plan_oops),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
            Text(
                text = errorMsg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
            if (state.error !is PlanError.ApiKeyMissing) {
                Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                NeoBrutalIconButton(
                    onClick = onClearError,
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.plan_label_dismiss_error),
                    backgroundColor = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun getErrorMessageAndIcon(error: PlanError): Pair<String, ImageVector> {
    return when (error) {
        is PlanError.ApiKeyMissing -> stringResource(R.string.plan_error_api_key_missing) to Icons.Rounded.VpnKey
        is PlanError.NetworkMissing -> stringResource(R.string.plan_error_network) to Icons.Rounded.WifiOff
        is PlanError.Unknown -> (error.message ?: stringResource(R.string.plan_error_unknown)) to Icons.Rounded.Warning
    }
}

// region Previews

@Preview(name = "Initial Light")
@Composable
private fun InitialStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            InitialState(
                textState = TextFieldValue(""),
                onTextChanged = {},
                onPlanClick = {},
            )
        }
    }
}

@Preview(name = "Initial Dark")
@Composable
private fun InitialStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            InitialState(
                textState = TextFieldValue("Coffee tour in Stockholm"),
                onTextChanged = {},
                onPlanClick = {},
            )
        }
    }
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
                        PlanStepUi(icon = StepIcon.TOOL_CALL, label = "Searching for specialty coffee shops"),
                        PlanStepUi(icon = StepIcon.TOOL_RESULT, label = "Found 4 coffee shops"),
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
                        PlanStepUi(icon = StepIcon.TOOL_CALL, label = "Calculating optimal route"),
                    ),
                    currentAction = "Calculating",
                ),
            )
        }
    }
}

@Preview(name = "Result Light")
@Composable
private fun ResultStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            ResultState(
                state = PlanUiState.Result(
                    steps = persistentListOf(
                        PlanStepUi(icon = StepIcon.TOOL_RESULT, label = "Found 3 stops"),
                    ),
                    plan = previewTripPlan(),
                ),
                onNewPlan = {},
            )
        }
    }
}

@Preview(name = "Result Dark")
@Composable
private fun ResultStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            ResultState(
                state = PlanUiState.Result(
                    steps = persistentListOf(
                        PlanStepUi(icon = StepIcon.TOOL_RESULT, label = "Found 3 stops"),
                    ),
                    plan = previewTripPlan(),
                ),
                onNewPlan = {},
            )
        }
    }
}

@Preview(name = "Error Light")
@Composable
private fun ErrorStateLightPreview() {
    SofaAiTheme(darkTheme = false) {
        Surface {
            ErrorState(
                state = PlanUiState.Error(error = PlanError.NetworkMissing),
                onClearError = {},
            )
        }
    }
}

@Preview(name = "Error Dark")
@Composable
private fun ErrorStateDarkPreview() {
    SofaAiTheme(darkTheme = true) {
        Surface {
            ErrorState(
                state = PlanUiState.Error(error = PlanError.ApiKeyMissing),
                onClearError = {},
            )
        }
    }
}

private fun previewTripPlan() = TripPlanUi(
    summary = "A delightful coffee tour through Stockholm's best specialty cafes, " +
        "starting from Södermalm and winding through the old town.",
    stops = persistentListOf(
        TripStopUi(
            name = "Drop Coffee",
            latitude = 59.3173,
            longitude = 18.0546,
            description = "Award-winning specialty roaster with minimalist Nordic vibes.",
            category = "Coffee Shop",
            orderIndex = 0,
        ),
        TripStopUi(
            name = "Johan & Nyström",
            latitude = 59.3210,
            longitude = 18.0710,
            description = "Popular chain known for single-origin beans and cozy atmosphere.",
            category = "Coffee Shop",
            orderIndex = 1,
        ),
        TripStopUi(
            name = "Café Pascal",
            latitude = 59.3390,
            longitude = 18.0580,
            description = "Trendy café serving expertly crafted espresso drinks.",
            category = "Coffee Shop",
            orderIndex = 2,
        ),
    ),
    totalDistanceKm = 2.4,
    totalWalkingMinutes = 29,
)

// endregion
