package se.onemanstudio.playaroundwithai.data.dream.domain.usecase

import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamImage
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamGeminiRepository
import javax.inject.Inject

class GenerateDreamImageUseCase @Inject constructor(
    private val repository: DreamGeminiRepository
) {
    @Suppress("ReturnCount")
    suspend operator fun invoke(description: String): Result<DreamImage> {
        if (description.isBlank()) {
            return Result.failure(IllegalArgumentException("Dream description cannot be blank"))
        }

        if (description.length > MAX_DREAM_LENGTH) {
            return Result.failure(IllegalArgumentException("Dream description exceeds maximum length of $MAX_DREAM_LENGTH characters"))
        }

        return repository.generateDreamImage(description)
    }
}
