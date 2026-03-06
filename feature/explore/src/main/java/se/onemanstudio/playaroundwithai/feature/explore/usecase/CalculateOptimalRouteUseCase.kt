package se.onemanstudio.playaroundwithai.feature.explore.usecase

import com.google.android.gms.maps.model.LatLng
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import se.onemanstudio.playaroundwithai.feature.explore.utils.calculatePathDistance
import se.onemanstudio.playaroundwithai.feature.explore.utils.permutations
import javax.inject.Inject
import kotlin.math.roundToInt

private const val WALKING_SPEED_METERS_PER_MIN = 83.0 // approx 5 km/h

data class OptimalRouteResult(
    val orderedPath: PersistentList<LatLng>,
    val distanceMeters: Int,
    val durationMinutes: Int,
)

private const val DISTANCE = 1_000

class CalculateOptimalRouteUseCase @Inject constructor() {

    operator fun invoke(startPoint: LatLng, pointsToVisit: List<LatLng>): OptimalRouteResult {
        val bestPermutation = permutations(pointsToVisit)
            .minByOrNull { path -> calculatePathDistance(startPoint, path) }
            ?: pointsToVisit

        val fullPath = (listOf(startPoint) + bestPermutation).toPersistentList()
        val totalDistanceKm = calculatePathDistance(startPoint, bestPermutation)
        val distanceMeters = (totalDistanceKm * DISTANCE).roundToInt()
        val durationMinutes = (distanceMeters / WALKING_SPEED_METERS_PER_MIN).roundToInt()

        return OptimalRouteResult(
            orderedPath = fullPath,
            distanceMeters = distanceMeters,
            durationMinutes = durationMinutes,
        )
    }
}
