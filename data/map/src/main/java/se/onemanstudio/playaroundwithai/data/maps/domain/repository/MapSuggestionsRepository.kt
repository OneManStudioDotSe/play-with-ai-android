package se.onemanstudio.playaroundwithai.data.maps.domain.repository

import se.onemanstudio.playaroundwithai.data.maps.domain.model.SuggestedPlace

interface MapSuggestionsRepository {
    suspend fun getSuggestedPlaces(
        latitude: Double,
        longitude: Double,
    ): Result<List<SuggestedPlace>>
}
