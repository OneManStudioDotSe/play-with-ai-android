package se.onemanstudio.playaroundwithai.data.explore.domain.repository

import se.onemanstudio.playaroundwithai.data.explore.domain.model.SuggestedPlace

interface ExploreSuggestionsRepository {
    suspend fun getSuggestedPlaces(
        latitude: Double,
        longitude: Double,
    ): Result<List<SuggestedPlace>>
}
