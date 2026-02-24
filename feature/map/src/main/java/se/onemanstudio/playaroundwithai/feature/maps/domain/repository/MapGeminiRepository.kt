package se.onemanstudio.playaroundwithai.feature.maps.domain.repository

import se.onemanstudio.playaroundwithai.core.network.model.GeminiModel
import se.onemanstudio.playaroundwithai.feature.maps.domain.model.SuggestedPlace

interface MapGeminiRepository {
    suspend fun getSuggestedPlaces(
        latitude: Double,
        longitude: Double,
        model: GeminiModel = GeminiModel.FLASH_PREVIEW,
    ): Result<List<SuggestedPlace>>
}
