package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import javax.inject.Inject

class UpdatePromptTextUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    suspend operator fun invoke(id: Long, text: String) {
        require(id > 0) { "Prompt ID must be positive, was $id" }
        require(text.isNotBlank()) { "Updated text must not be blank" }

        repository.updatePromptText(id, text)
    }
}
