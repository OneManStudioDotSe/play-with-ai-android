package se.onemanstudio.playaroundwithai.feature.maps.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.feature.maps.data.api.MapApiService
import se.onemanstudio.playaroundwithai.feature.maps.data.mapper.toDomain
import se.onemanstudio.playaroundwithai.feature.maps.domain.model.MapItem
import se.onemanstudio.playaroundwithai.feature.maps.domain.repository.MapRepository
import timber.log.Timber
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val mapApiService: MapApiService,
) : MapRepository {
    override suspend fun getMapItems(count: Int, centerLat: Double, centerLng: Double): List<MapItem> = withContext(Dispatchers.IO) {
        Timber.d("MapRepo - Fetching $count map items from API centered at ($centerLat, $centerLng)...")
        val items = mapApiService.getMapItems(count, centerLat, centerLng).map { it.toDomain() }
        Timber.d("MapRepo - Received ${items.size} map items")
        items
    }
}
