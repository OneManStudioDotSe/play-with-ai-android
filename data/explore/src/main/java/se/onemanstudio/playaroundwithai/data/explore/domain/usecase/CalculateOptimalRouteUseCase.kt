package se.onemanstudio.playaroundwithai.data.explore.domain.usecase

import se.onemanstudio.playaroundwithai.data.explore.domain.util.calculatePathDistance
import se.onemanstudio.playaroundwithai.data.explore.domain.util.permutations
import javax.inject.Inject
import kotlin.math.roundToInt

private const val WALKING_SPEED_METERS_PER_MIN = 83.0 // approx 5 km/h
private const val METERS_PER_KM = 1_000

data class OptimalRouteResult(
    val orderedPath: List<Pair<Double, Double>>,
    val distanceMeters: Int,
    val durationMinutes: Int,
)

class CalculateOptimalRouteUseCase @Inject constructor() {

    operator fun invoke(
        startLat: Double,
        startLng: Double,
        pointsToVisit: List<Pair<Double, Double>>,
    ): OptimalRouteResult {
        val bestPermutation = permutations(pointsToVisit)
            .minByOrNull { path -> calculatePathDistance(startLat, startLng, path) }
            ?: pointsToVisit

        val fullPath = listOf(startLat to startLng) + bestPermutation
        val totalDistanceKm = calculatePathDistance(startLat, startLng, bestPermutation)
        val distanceMeters = (totalDistanceKm * METERS_PER_KM).roundToInt()
        val durationMinutes = (distanceMeters / WALKING_SPEED_METERS_PER_MIN).roundToInt()

        return OptimalRouteResult(
            orderedPath = fullPath,
            distanceMeters = distanceMeters,
            durationMinutes = durationMinutes,
        )
    }
}
