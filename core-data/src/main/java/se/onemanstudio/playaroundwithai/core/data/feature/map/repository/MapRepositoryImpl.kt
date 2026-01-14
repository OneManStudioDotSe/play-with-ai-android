package se.onemanstudio.playaroundwithai.core.data.feature.map.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.core.data.feature.map.api.MapApiService
import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.toDomain
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.feature.map.repository.MapRepository
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val mapApiService: MapApiService,
) : MapRepository {
    override suspend fun getMapItems(count: Int): List<MapItem> = withContext(Dispatchers.IO) {
        mapApiService.getMapItems(count).map { it.toDomain() }
    }
}
