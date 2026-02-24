package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.GeminiModel
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import javax.inject.Inject

internal const val MAX_PROMPT_LENGTH = 50_000
internal const val MAX_FILE_TEXT_LENGTH = 100_000

class AskAiUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    @Suppress("ReturnCount")
    suspend operator fun invoke(
        prompt: String,
        imageBytes: ByteArray? = null,
        fileText: String? = null,
        analysisType: AnalysisType? = null,
        model: GeminiModel = GeminiModel.FLASH_PREVIEW,
    ): Result<String> {
        if (prompt.isBlank() && imageBytes == null && fileText == null) {
            return Result.failure(IllegalArgumentException("Prompt and attachments cannot all be empty"))
        }

        if (prompt.length > MAX_PROMPT_LENGTH) {
            return Result.failure(IllegalArgumentException("Prompt exceeds maximum length of $MAX_PROMPT_LENGTH characters"))
        }

        if (fileText != null && fileText.length > MAX_FILE_TEXT_LENGTH) {
            return Result.failure(IllegalArgumentException("File content exceeds maximum length of $MAX_FILE_TEXT_LENGTH characters"))
        }

        return repository.getAiResponse(prompt, imageBytes, fileText, analysisType, model)
    }
}
