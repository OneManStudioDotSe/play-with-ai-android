package se.onemanstudio.playaroundwithai.data.chat.domain.usecase

import se.onemanstudio.playaroundwithai.data.chat.domain.repository.PromptRepository
import javax.inject.Inject

class UpdatePromptTextUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    suspend operator fun invoke(id: Long, text: String): Result<Unit> {
        if (id <= 0) return Result.failure(IllegalArgumentException("Prompt ID must be positive, was $id"))
        if (text.isBlank()) return Result.failure(IllegalArgumentException("Updated text must not be blank"))
        return runCatching { repository.updatePromptText(id, text) }
    }
}
