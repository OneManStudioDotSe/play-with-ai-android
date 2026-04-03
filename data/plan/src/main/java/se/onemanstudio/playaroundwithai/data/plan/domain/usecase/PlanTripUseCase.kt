package se.onemanstudio.playaroundwithai.data.plan.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import se.onemanstudio.playaroundwithai.core.network.utils.MAX_LATITUDE
import se.onemanstudio.playaroundwithai.core.network.utils.MAX_LONGITUDE
import se.onemanstudio.playaroundwithai.core.network.utils.MIN_LATITUDE
import se.onemanstudio.playaroundwithai.core.network.utils.MIN_LONGITUDE
import se.onemanstudio.playaroundwithai.data.plan.domain.model.PlanEvent
import se.onemanstudio.playaroundwithai.data.plan.domain.repository.TripPlannerRepository
import javax.inject.Inject

private const val MAX_GOAL_LENGTH = 1_000

class PlanTripUseCase @Inject constructor(
    private val repository: TripPlannerRepository,
) {
    @Suppress("ReturnCount")
    operator fun invoke(goal: String, latitude: Double, longitude: Double): Flow<PlanEvent> {
        val validationError = validateInput(goal, latitude, longitude)
        if (validationError != null) return flowOf(PlanEvent.Error(validationError))

        return repository.planTrip(goal, latitude, longitude)
    }

    private fun validateInput(goal: String, latitude: Double, longitude: Double): String? = when {
        goal.isBlank() -> "Please describe your trip idea"
        goal.length > MAX_GOAL_LENGTH -> "Trip description is too long (max $MAX_GOAL_LENGTH characters)"
        latitude !in MIN_LATITUDE..MAX_LATITUDE || longitude !in MIN_LONGITUDE..MAX_LONGITUDE -> "Invalid location coordinates"
        else -> null
    }
}
