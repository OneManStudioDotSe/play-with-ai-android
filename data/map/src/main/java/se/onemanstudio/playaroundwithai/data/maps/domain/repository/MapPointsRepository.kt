package se.onemanstudio.playaroundwithai.data.maps.domain.repository

import se.onemanstudio.playaroundwithai.data.maps.domain.model.MapItem

interface MapPointsRepository {
    suspend fun getMapItems(
        count: Int,
        centerLat: Double,
        centerLng: Double
    ): List<MapItem>
}
