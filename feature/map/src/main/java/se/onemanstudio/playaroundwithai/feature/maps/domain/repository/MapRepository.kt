package se.onemanstudio.playaroundwithai.feature.maps.domain.repository

import se.onemanstudio.playaroundwithai.feature.maps.domain.model.MapItem

interface MapRepository {
    suspend fun getMapItems(count: Int, centerLat: Double, centerLng: Double): List<MapItem>
}
