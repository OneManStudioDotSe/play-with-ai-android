package se.onemanstudio.playaroundwithai.feature.maps.domain.repository

import se.onemanstudio.playaroundwithai.feature.maps.domain.model.SuggestedPlace

interface MapGeminiRepository {
    suspend fun getSuggestedPlaces(
        latitude: Double,
        longitude: Double,
    ): Result<List<SuggestedPlace>>
}
