package se.onemanstudio.playaroundwithai.core.data.feature.map.api

import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.MapItemDto

/**
 * Interface for a remote service that provides map-related data.
 */
interface MapApiService {
    /**
     * Fetches a list of map items from the remote service.
     *
     * @param count The number of items to fetch.
     * @return A list of [MapItemDto] objects.
     */
    suspend fun getMapItems(count: Int): List<MapItemDto>
}
