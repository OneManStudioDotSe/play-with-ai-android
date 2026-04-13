package se.onemanstudio.playaroundwithai.data.explore.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.data.explore.data.api.ExploreApiService
import se.onemanstudio.playaroundwithai.data.explore.data.mapper.toDomain
import se.onemanstudio.playaroundwithai.data.explore.domain.model.ExploreItem
import se.onemanstudio.playaroundwithai.data.explore.domain.repository.ExplorePointsRepository
import timber.log.Timber
import javax.inject.Inject

class ExplorePointsRepositoryImpl @Inject constructor(
    private val exploreApiService: ExploreApiService,
) : ExplorePointsRepository {
    override suspend fun getExploreItems(count: Int, centerLat: Double, centerLng: Double): List<ExploreItem> = withContext(Dispatchers.IO) {
        exploreApiService.getExploreItems(count, centerLat, centerLng).map { it.toDomain() }
    }
}
