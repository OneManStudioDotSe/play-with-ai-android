package se.onemanstudio.playaroundwithai.data.explore.domain.usecase

import se.onemanstudio.playaroundwithai.core.network.utils.MAX_LATITUDE
import se.onemanstudio.playaroundwithai.core.network.utils.MAX_LONGITUDE
import se.onemanstudio.playaroundwithai.data.explore.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.data.explore.domain.repository.ExploreSuggestionsRepository
import javax.inject.Inject

class GetSuggestedPlacesUseCase @Inject constructor(
    private val exploreSuggestionsRepository: ExploreSuggestionsRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Result<List<SuggestedPlace>> {
        if (latitude !in -MAX_LATITUDE..MAX_LATITUDE || longitude !in -MAX_LONGITUDE..MAX_LONGITUDE) {
            val message = when {
                latitude !in -MAX_LATITUDE..MAX_LATITUDE -> "Latitude must be between -$MAX_LATITUDE and $MAX_LATITUDE, was $latitude"
                else -> "Longitude must be between -$MAX_LONGITUDE and $MAX_LONGITUDE, was $longitude"
            }
            return Result.failure(IllegalArgumentException(message))
        }

        return exploreSuggestionsRepository.getSuggestedPlaces(latitude, longitude)
    }
}
