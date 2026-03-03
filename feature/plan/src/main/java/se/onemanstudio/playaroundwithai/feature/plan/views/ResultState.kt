@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.feature.plan.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import se.onemanstudio.playaroundwithai.core.ui.sofa.MarkerText
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalButton
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalCard
import se.onemanstudio.playaroundwithai.core.ui.sofa.NeoBrutalChip
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.electricBlue
import se.onemanstudio.playaroundwithai.core.ui.theme.energeticOrange
import se.onemanstudio.playaroundwithai.core.ui.theme.solarYellow
import se.onemanstudio.playaroundwithai.core.ui.theme.vividPink
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.feature.plan.R
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.ACCENT_COLOR_COUNT
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.MAP_HEIGHT_MAX
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.MAP_HEIGHT_MIN
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.MAP_ZOOM
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.ORDER_BADGE_SIZE
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.POLYLINE_WIDTH
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.RESULT_BUTTON_DELAY_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.RESULT_ITINERARY_DELAY_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.RESULT_METRICS_DELAY_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.RESULT_STOP_STAGGER_MS
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.RESULT_SUMMARY_DELAY_MS
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanStepUi
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState
import se.onemanstudio.playaroundwithai.feature.plan.states.StepIcon
import se.onemanstudio.playaroundwithai.feature.plan.states.TripPlanUi
import se.onemanstudio.playaroundwithai.feature.plan.states.TripStopUi

private val accentColors = listOf(electricBlue, vividPink, zestyLime, solarYellow, energeticOrange)

@Composable
internal fun ResultState(
    state: PlanUiState.Result,
    onNewPlan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Map section
        if (state.plan.stops.isNotEmpty()) {
            PhasedVisibility(delayMs = 0) {
                Column(modifier = Modifier.padding(horizontal = Dimensions.paddingExtraLarge)) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

                    MarkerText(
                        text = stringResource(R.string.plan_your_route),
                        lineColor = electricBlue,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

                    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                        TripMap(
                            stops = state.plan.stops,
                            modifier = Modifier.clip(RectangleShape),
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(Dimensions.paddingExtraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Trip Summary section
            PhasedVisibility(delayMs = RESULT_SUMMARY_DELAY_MS) {
                Column {
                    MarkerText(
                        text = stringResource(R.string.plan_trip_summary),
                        lineColor = vividPink,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

                    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimensions.paddingLarge)) {
                            Text(
                                text = state.plan.summary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            // Itinerary section
            PhasedVisibility(delayMs = RESULT_ITINERARY_DELAY_MS) {
                MarkerText(
                    text = stringResource(R.string.plan_your_itinerary),
                    lineColor = zestyLime,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

            state.plan.stops.forEachIndexed { index, stop ->
                val stopTransitionState = remember(stop) {
                    MutableTransitionState(false).apply { targetState = true }
                }

                AnimatedVisibility(
                    visibleState = stopTransitionState,
                    enter = slideInHorizontally(
                        initialOffsetX = { it / 3 },
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = RESULT_ITINERARY_DELAY_MS + (index * RESULT_STOP_STAGGER_MS),
                        ),
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = RESULT_ITINERARY_DELAY_MS + (index * RESULT_STOP_STAGGER_MS),
                        ),
                    ),
                ) {
                    StopCard(stop = stop)
                }

                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingLarge))

            // Metrics card
            PhasedVisibility(delayMs = RESULT_METRICS_DELAY_MS) {
                MetricsCard(
                    distanceKm = state.plan.totalDistanceKm,
                    walkingMinutes = state.plan.totalWalkingMinutes,
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

            // New plan button
            val buttonTransitionState = remember {
                MutableTransitionState(false).apply { targetState = true }
            }

            AnimatedVisibility(
                visibleState = buttonTransitionState,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 300, delayMillis = RESULT_BUTTON_DELAY_MS),
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(durationMillis = 300, delayMillis = RESULT_BUTTON_DELAY_MS),
                ),
            ) {
                NeoBrutalButton(
                    text = stringResource(R.string.plan_new_plan_button),
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    onClick = onNewPlan,
                )
            }

            if (state.steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))

                AgentLogSection(steps = state.steps)
            }
        }
    }
}

@Composable
private fun PhasedVisibility(
    delayMs: Int,
    content: @Composable () -> Unit,
) {
    val transitionState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    AnimatedVisibility(
        visibleState = transitionState,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 300, delayMillis = delayMs),
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(durationMillis = 300, delayMillis = delayMs),
        ),
    ) {
        content()
    }
}

@Composable
private fun TripMap(
    stops: PersistentList<TripStopUi>,
    modifier: Modifier = Modifier,
) {
    val firstStop = stops.first()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(firstStop.latitude, firstStop.longitude),
            MAP_ZOOM,
        )
    }

    val routePoints = stops.map { LatLng(it.latitude, it.longitude) }

    GoogleMap(
        modifier = modifier
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
            OrderBadge(orderIndex = stop.orderIndex)
            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stop.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (stop.category.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
                    NeoBrutalChip(
                        text = stop.category,
                        onClick = {},
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
private fun OrderBadge(orderIndex: Int) {
    val badgeColor = accentColors[orderIndex % ACCENT_COLOR_COUNT]

    Box(
        modifier = Modifier
            .size(ORDER_BADGE_SIZE.dp)
            .background(badgeColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${orderIndex + 1}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MetricsCard(distanceKm: Double, walkingMinutes: Int) {
    NeoBrutalCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                    contentDescription = null,
                    tint = electricBlue,
                    modifier = Modifier.size(Dimensions.iconSizeMedium),
                )
                Spacer(modifier = Modifier.width(Dimensions.paddingSmall))
                Text(
                    text = stringResource(R.string.plan_distance_value, distanceKm),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            VerticalDivider(
                modifier = Modifier.height(Dimensions.iconSizeMedium),
                thickness = Dimensions.borderStrokeSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = energeticOrange,
                    modifier = Modifier.size(Dimensions.iconSizeMedium),
                )
                Spacer(modifier = Modifier.width(Dimensions.paddingSmall))
                Text(
                    text = stringResource(R.string.plan_time_value, walkingMinutes),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
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
                        PlanStepUi(icon = StepIcon.TOOL_RESULT, label = "Found 3 stops", toolName = "search_places"),
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
                        PlanStepUi(icon = StepIcon.TOOL_RESULT, label = "Found 3 stops", toolName = "search_places"),
                    ),
                    plan = previewTripPlan(),
                ),
                onNewPlan = {},
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
