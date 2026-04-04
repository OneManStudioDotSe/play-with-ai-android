package se.onemanstudio.playaroundwithai.data.explore.domain.usecase

import se.onemanstudio.playaroundwithai.core.network.utils.permutations
import se.onemanstudio.playaroundwithai.data.explore.domain.utils.calculatePathDistance
import javax.inject.Inject
import kotlin.math.roundToInt

private const val METERS_PER_KM = 1_000
private const val MINUTES_PER_HOUR = 60.0

data class OptimalRouteResult(
    val orderedPath: List<Pair<Double, Double>>,
    val distanceMeters: Int,
    val durationMinutes: Int,
)

class CalculateOptimalRouteUseCase @Inject constructor() {

    operator fun invoke(startLat: Double, startLng: Double, pointsToVisit: List<Pair<Double, Double>>, walkingSpeedKmh: Double): OptimalRouteResult {
        val bestPermutation = permutations(pointsToVisit)
            .minByOrNull { path -> calculatePathDistance(startLat, startLng, path) }
            ?: pointsToVisit

        val fullPath = listOf(startLat to startLng) + bestPermutation
        val totalDistanceKm = calculatePathDistance(startLat, startLng, bestPermutation)
        val distanceMeters = (totalDistanceKm * METERS_PER_KM).roundToInt()
        val durationMinutes = (totalDistanceKm / walkingSpeedKmh * MINUTES_PER_HOUR).roundToInt()

        return OptimalRouteResult(
            orderedPath = fullPath,
            distanceMeters = distanceMeters,
            durationMinutes = durationMinutes,
        )
    }
}
