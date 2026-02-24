package se.onemanstudio.playaroundwithai.feature.maps.domain.usecase

import se.onemanstudio.playaroundwithai.feature.maps.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.feature.maps.domain.repository.MapGeminiRepository
import javax.inject.Inject

private const val MAX_LATITUDE = 90.0
private const val MAX_LONGITUDE = 180.0

class GetSuggestedPlacesUseCase @Inject constructor(
    private val mapGeminiRepository: MapGeminiRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
    ): Result<List<SuggestedPlace>> {
        if (latitude !in -MAX_LATITUDE..MAX_LATITUDE || longitude !in -MAX_LONGITUDE..MAX_LONGITUDE) {
            val message = when {
                latitude !in -MAX_LATITUDE..MAX_LATITUDE -> "Latitude must be between -$MAX_LATITUDE and $MAX_LATITUDE, was $latitude"
                else -> "Longitude must be between -$MAX_LONGITUDE and $MAX_LONGITUDE, was $longitude"
            }
            return Result.failure(IllegalArgumentException(message))
        }

        return mapGeminiRepository.getSuggestedPlaces(latitude, longitude)
    }
}
