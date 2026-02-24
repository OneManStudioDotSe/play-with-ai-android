package se.onemanstudio.playaroundwithai.feature.maps.data.api

import se.onemanstudio.playaroundwithai.feature.maps.data.dto.MapItemDto

interface MapApiService {
    suspend fun getMapItems(count: Int, centerLat: Double, centerLng: Double): List<MapItemDto>
}
