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
        return geminiRepository.getSuggestedPlaces(latitude, longitude, model)
    }
}
