package se.onemanstudio.playaroundwithai.core.data.feature.map.repository

import se.onemanstudio.playaroundwithai.core.data.model.MapItem

interface MapRepository {
    suspend fun getMapItems(count: Int): List<MapItem>
}
