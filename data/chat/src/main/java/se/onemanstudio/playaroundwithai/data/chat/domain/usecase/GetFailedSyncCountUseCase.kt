package se.onemanstudio.playaroundwithai.data.chat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.PromptRepository
import javax.inject.Inject

class GetFailedSyncCountUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    operator fun invoke(): Flow<Int> = repository.getFailedSyncCount()
        .distinctUntilChanged()
}
