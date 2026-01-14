package se.onemanstudio.playaroundwithai.core.domain.usecase

import se.onemanstudio.playaroundwithai.core.domain.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.repository.GeminiDomainRepository
import javax.inject.Inject

class GenerateContentUseCase @Inject constructor(
    private val repository: GeminiDomainRepository
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
