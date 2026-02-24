package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import javax.inject.Inject

private const val MAX_PROMPT_TEXT_LENGTH = 50_000

class SavePromptUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    suspend operator fun invoke(prompt: Prompt): Long {
        require(prompt.text.isNotBlank()) { "Prompt text must not be blank" }
        require(prompt.text.length <= MAX_PROMPT_TEXT_LENGTH) { "Prompt text exceeds maximum length of $MAX_PROMPT_TEXT_LENGTH" }

        return repository.savePrompt(prompt)
    }
}
