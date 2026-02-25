package se.onemanstudio.playaroundwithai.data.explore.data.api

import se.onemanstudio.playaroundwithai.data.explore.data.dto.ExploreItemDto

interface ExploreApiService {
    suspend fun getExploreItems(count: Int, centerLat: Double, centerLng: Double): List<ExploreItemDto>
}
