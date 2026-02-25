package se.onemanstudio.playaroundwithai.data.agents.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import se.onemanstudio.playaroundwithai.data.agents.domain.model.AgentEvent
import se.onemanstudio.playaroundwithai.data.agents.domain.repository.TripPlannerRepository
import javax.inject.Inject

private const val MAX_GOAL_LENGTH = 1_000
private const val MIN_LATITUDE = -90.0
private const val MAX_LATITUDE = 90.0
private const val MIN_LONGITUDE = -180.0
private const val MAX_LONGITUDE = 180.0

class PlanTripUseCase @Inject constructor(
    private val repository: TripPlannerRepository,
) {
    @Suppress("ReturnCount")
    operator fun invoke(goal: String, latitude: Double, longitude: Double): Flow<AgentEvent> {
        val validationError = validateInput(goal, latitude, longitude)
        if (validationError != null) return flowOf(AgentEvent.Error(validationError))

        return repository.planTrip(goal, latitude, longitude)
    }

    private fun validateInput(goal: String, latitude: Double, longitude: Double): String? = when {
        goal.isBlank() -> "Please describe your trip idea"
        goal.length > MAX_GOAL_LENGTH -> "Trip description is too long (max $MAX_GOAL_LENGTH characters)"
        latitude !in MIN_LATITUDE..MAX_LATITUDE || longitude !in MIN_LONGITUDE..MAX_LONGITUDE -> "Invalid location coordinates"
        else -> null
    }
}
