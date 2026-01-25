package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import javax.inject.Inject

class GetSyncStateUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isSyncing()
}
