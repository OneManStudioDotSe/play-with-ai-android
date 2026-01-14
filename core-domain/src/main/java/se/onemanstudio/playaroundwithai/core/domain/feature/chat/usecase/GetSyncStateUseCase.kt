package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import javax.inject.Inject

class GetSyncStateUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isSyncing()
    }
}
