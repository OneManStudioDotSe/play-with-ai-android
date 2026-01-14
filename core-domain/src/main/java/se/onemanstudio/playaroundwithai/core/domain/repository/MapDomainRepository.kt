package se.onemanstudio.playaroundwithai.core.domain.repository

import se.onemanstudio.playaroundwithai.core.domain.model.MapItem

interface MapDomainRepository {
    suspend fun getMapItems(count: Int): List<MapItem>
}
