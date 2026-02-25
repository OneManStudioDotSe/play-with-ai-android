package se.onemanstudio.playaroundwithai.data.agents.domain.repository

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.data.agents.domain.model.AgentEvent

interface TripPlannerRepository {
    fun planTrip(goal: String, latitude: Double, longitude: Double): Flow<AgentEvent>
}
