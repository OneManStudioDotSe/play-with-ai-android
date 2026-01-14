package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import javax.inject.Inject

class GenerateContentUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(
        prompt: String,
        imageBytes: ByteArray? = null,
        fileText: String? = null,
        analysisType: AnalysisType? = null
    ): Result<String> {
        if (prompt.isBlank() && imageBytes == null && fileText == null) {
            return Result.failure(IllegalArgumentException("Prompt and attachments cannot all be empty"))
        }
        return repository.generateContent(prompt, imageBytes, fileText, analysisType)
    }
}
