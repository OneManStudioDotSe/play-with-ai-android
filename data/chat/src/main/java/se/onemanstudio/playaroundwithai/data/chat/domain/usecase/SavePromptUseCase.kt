package se.onemanstudio.playaroundwithai.data.chat.domain.usecase

import se.onemanstudio.playaroundwithai.data.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.PromptRepository
import javax.inject.Inject

private const val MAX_PROMPT_TEXT_LENGTH = 50_000

class SavePromptUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    @Suppress("ReturnCount")
    suspend operator fun invoke(prompt: Prompt): Result<Long> {
        if (prompt.text.isBlank()) {
            return Result.failure(IllegalArgumentException("Prompt text must not be blank"))
        }
        if (prompt.text.length > MAX_PROMPT_TEXT_LENGTH) {
            return Result.failure(IllegalArgumentException("Prompt text exceeds maximum length of $MAX_PROMPT_TEXT_LENGTH"))
        }
        return runCatching { repository.savePrompt(prompt) }
    }
}
