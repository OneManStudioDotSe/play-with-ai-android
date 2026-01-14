package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import javax.inject.Inject

class SavePromptUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(promptText: String) {
        repository.savePrompt(promptText)
    }
}
