package se.onemanstudio.playaroundwithai.feature.plan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import se.onemanstudio.playaroundwithai.core.ui.theme.Alphas
import se.onemanstudio.playaroundwithai.core.ui.theme.Dimensions
import se.onemanstudio.playaroundwithai.core.ui.theme.SofaAiTheme
import se.onemanstudio.playaroundwithai.core.ui.theme.zestyLime
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.MAP_HEIGHT_MAX
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.MAP_HEIGHT_MIN
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.MAP_ZOOM
import se.onemanstudio.playaroundwithai.feature.plan.PlanConstants.POLYLINE_WIDTH
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanStepUi
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState
import se.onemanstudio.playaroundwithai.feature.plan.states.StepIcon
import se.onemanstudio.playaroundwithai.feature.plan.states.TripPlanUi
import se.onemanstudio.playaroundwithai.feature.plan.states.TripStopUi
import se.onemanstudio.playaroundwithai.feature.plan.R

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

            MarkerText(
                text = stringResource(R.string.plan_your_itinerary),
                lineColor = zestyLime,
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.medium),
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
