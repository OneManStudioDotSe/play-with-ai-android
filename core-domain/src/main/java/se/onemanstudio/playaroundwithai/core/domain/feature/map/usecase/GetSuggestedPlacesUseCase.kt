package se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.GeminiModel
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace
import javax.inject.Inject

class GetSuggestedPlacesUseCase @Inject constructor(
    private val geminiRepository: GeminiRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        model: GeminiModel = GeminiModel.FLASH_PREVIEW,
    ): Result<List<SuggestedPlace>> {
        if (latitude !in -MAX_LATITUDE..MAX_LATITUDE) {
            return Result.failure(IllegalArgumentException("Latitude must be between -$MAX_LATITUDE and $MAX_LATITUDE, was $latitude"))
        }
        if (longitude !in -MAX_LONGITUDE..MAX_LONGITUDE) {
            return Result.failure(IllegalArgumentException("Longitude must be between -$MAX_LONGITUDE and $MAX_LONGITUDE, was $longitude"))
        }

        return geminiRepository.getSuggestedPlaces(latitude, longitude, model)
    }

    companion object {
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
    }
}
