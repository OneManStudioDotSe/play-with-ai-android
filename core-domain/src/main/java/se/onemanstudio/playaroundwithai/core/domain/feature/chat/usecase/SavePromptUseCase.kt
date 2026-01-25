package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import javax.inject.Inject

class SavePromptUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    suspend operator fun invoke(prompt: Prompt) {
        repository.savePrompt(prompt)
    }
}
