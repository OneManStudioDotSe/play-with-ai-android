package se.onemanstudio.playaroundwithai.feature.agents.states

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList

@Immutable
sealed interface AgentUiState {
    data object Initial : AgentUiState

    data class Running(
        val steps: PersistentList<AgentStepUi>,
        val currentAction: String,
    ) : AgentUiState

    data class Result(
        val steps: PersistentList<AgentStepUi>,
        val plan: TripPlanUi,
    ) : AgentUiState

    data class Error(val error: AgentError) : AgentUiState
}

@Immutable
data class AgentStepUi(
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
sealed interface AgentError {
    data object ApiKeyMissing : AgentError
    data object NetworkMissing : AgentError
    data class Unknown(val message: String?) : AgentError
}
