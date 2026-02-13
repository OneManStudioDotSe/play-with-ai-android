package se.onemanstudio.playaroundwithai.core.data.feature.map.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.core.data.feature.map.api.MapApiService
import se.onemanstudio.playaroundwithai.core.data.feature.map.mapper.toDomain
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.feature.map.repository.MapRepository
import timber.log.Timber
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val mapApiService: MapApiService,
) : MapRepository {
    override suspend fun getMapItems(count: Int): List<MapItem> = withContext(Dispatchers.IO) {
        Timber.d("MapRepo - Fetching $count map items from API...")
        val items = mapApiService.getMapItems(count).map { it.toDomain() }
        Timber.d("MapRepo - Received ${items.size} map items")
        items
    }
}
