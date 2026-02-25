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
    suspend operator fun invoke(count: Int, centerLat: Double, centerLng: Double): List<ExploreItem> {
        require(count in 1..MAX_ITEM_COUNT) { "Count must be between 1 and $MAX_ITEM_COUNT, was $count" }
        require(centerLat in -MAX_LATITUDE..MAX_LATITUDE) { "Latitude must be between -$MAX_LATITUDE and $MAX_LATITUDE, was $centerLat" }
        require(centerLng in -MAX_LONGITUDE..MAX_LONGITUDE) { "Longitude must be between -$MAX_LONGITUDE and $MAX_LONGITUDE, was $centerLng" }

        return repository.getExploreItems(count, centerLat, centerLng)
    }
}
