package se.onemanstudio.playaroundwithai.feature.chat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import se.onemanstudio.playaroundwithai.feature.chat.domain.repository.PromptRepository
import javax.inject.Inject

private const val SYNC_STATE_DEBOUNCE_MS = 300L

class GetSyncStateUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.isSyncing()
        .debounce(SYNC_STATE_DEBOUNCE_MS)
        .distinctUntilChanged()
}
