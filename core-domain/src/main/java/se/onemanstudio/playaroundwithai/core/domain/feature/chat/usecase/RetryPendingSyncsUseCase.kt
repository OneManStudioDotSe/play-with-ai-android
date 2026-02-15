package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import javax.inject.Inject

class RetryPendingSyncsUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    suspend operator fun invoke() {
        repository.retryPendingSyncs()
    }
}
