package se.onemanstudio.playaroundwithai.data.maps.data.api

import se.onemanstudio.playaroundwithai.data.maps.data.dto.MapItemDto

interface MapApiService {
    suspend fun getMapItems(count: Int, centerLat: Double, centerLng: Double): List<MapItemDto>
}
