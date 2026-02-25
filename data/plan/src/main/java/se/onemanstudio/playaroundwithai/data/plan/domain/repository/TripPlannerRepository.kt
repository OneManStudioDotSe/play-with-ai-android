package se.onemanstudio.playaroundwithai.data.plan.domain.repository

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.data.plan.domain.model.PlanEvent

interface TripPlannerRepository {
    fun planTrip(goal: String, latitude: Double, longitude: Double): Flow<PlanEvent>
}
