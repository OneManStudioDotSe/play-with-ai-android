package se.onemanstudio.playaroundwithai.core.domain.feature.map.repository

import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem

interface MapRepository {
    suspend fun getMapItems(count: Int): List<MapItem>
}
