package se.onemanstudio.playaroundwithai.data.explore.domain.usecase

import se.onemanstudio.playaroundwithai.data.explore.domain.model.ExploreItem
import se.onemanstudio.playaroundwithai.data.explore.domain.repository.ExplorePointsRepository
import javax.inject.Inject

internal const val MAX_ITEM_COUNT = 100
private const val MAX_LATITUDE = 90.0
private const val MAX_LONGITUDE = 180.0

class GetExploreItemsUseCase @Inject constructor(
    private val repository: ExplorePointsRepository
) {
    suspend operator fun invoke(count: Int, centerLat: Double, centerLng: Double): Result<List<ExploreItem>> {
        if (count !in 1..MAX_ITEM_COUNT) {
            return Result.failure(IllegalArgumentException("Count must be between 1 and $MAX_ITEM_COUNT, was $count"))
        }
        if (centerLat !in -MAX_LATITUDE..MAX_LATITUDE) {
            return Result.failure(IllegalArgumentException("Latitude must be between -$MAX_LATITUDE and $MAX_LATITUDE, was $centerLat"))
        }
        if (centerLng !in -MAX_LONGITUDE..MAX_LONGITUDE) {
            return Result.failure(IllegalArgumentException("Longitude must be between -$MAX_LONGITUDE and $MAX_LONGITUDE, was $centerLng"))
        }
        return runCatching { repository.getExploreItems(count, centerLat, centerLng) }
    }
}
