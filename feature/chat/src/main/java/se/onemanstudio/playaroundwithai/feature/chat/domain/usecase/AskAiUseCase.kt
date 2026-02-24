package se.onemanstudio.playaroundwithai.feature.chat.domain.usecase

import se.onemanstudio.playaroundwithai.core.network.model.GeminiModel
import se.onemanstudio.playaroundwithai.feature.chat.domain.model.AnalysisType
import se.onemanstudio.playaroundwithai.feature.chat.domain.repository.ChatGeminiRepository
import javax.inject.Inject

internal const val MAX_PROMPT_LENGTH = 50_000
internal const val MAX_FILE_TEXT_LENGTH = 100_000

class AskAiUseCase @Inject constructor(
    private val repository: ChatGeminiRepository
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
