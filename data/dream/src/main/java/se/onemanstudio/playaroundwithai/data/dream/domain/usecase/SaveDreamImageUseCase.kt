package se.onemanstudio.playaroundwithai.data.dream.domain.usecase

import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import javax.inject.Inject

class SaveDreamImageUseCase @Inject constructor(
    private val repository: DreamRepository
) {
    suspend operator fun invoke(dreamId: Long, imageBytes: ByteArray, mimeType: String, artistName: String): String {
        require(dreamId > 0) { "Dream ID must be positive" }
        require(imageBytes.isNotEmpty()) { "Image bytes must not be empty" }
        require(mimeType.isNotBlank()) { "MIME type must not be blank" }

        return repository.saveDreamImage(dreamId, imageBytes, mimeType, artistName)
    }
}
