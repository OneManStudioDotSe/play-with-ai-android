package se.onemanstudio.playaroundwithai.feature.plan.states

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList

@Immutable
sealed interface PlanUiState {
    data object Initial : PlanUiState

    data class Running(
        val steps: PersistentList<PlanStepUi>,
        val currentAction: String,
    ) : PlanUiState

    data class Result(
        val steps: PersistentList<PlanStepUi>,
        val plan: TripPlanUi,
    ) : PlanUiState

    data class Error(val error: PlanError) : PlanUiState
}

@Immutable
data class PlanStepUi(
    val icon: StepIcon,
    val label: String,
)

enum class StepIcon { THINKING, TOOL_CALL, TOOL_RESULT }

@Immutable
data class TripPlanUi(
    val summary: String,
    val stops: PersistentList<TripStopUi>,
    val totalDistanceKm: Double,
    val totalWalkingMinutes: Int,
)

@Immutable
data class TripStopUi(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val category: String,
    val orderIndex: Int,
)

@Immutable
sealed interface PlanError {
    data object ApiKeyMissing : PlanError
    data object NetworkMissing : PlanError
    data class Unknown(val message: String?) : PlanError
}
