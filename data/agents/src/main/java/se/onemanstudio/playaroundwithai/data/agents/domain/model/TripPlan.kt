package se.onemanstudio.playaroundwithai.data.agents.domain.model

data class TripPlan(
    val summary: String,
    val stops: List<TripStop>,
    val totalDistanceKm: Double,
    val totalWalkingMinutes: Int,
)

data class TripStop(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val category: String,
    val orderIndex: Int,
)
