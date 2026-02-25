package se.onemanstudio.playaroundwithai.data.explore.domain.repository

import se.onemanstudio.playaroundwithai.data.explore.domain.model.ExploreItem

interface ExplorePointsRepository {
    suspend fun getExploreItems(
        count: Int,
        centerLat: Double,
        centerLng: Double
    ): List<ExploreItem>
}
