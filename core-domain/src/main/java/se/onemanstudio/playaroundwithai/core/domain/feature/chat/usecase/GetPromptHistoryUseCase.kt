package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import javax.inject.Inject

class GetPromptHistoryUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    operator fun invoke(): Flow<List<Prompt>> = repository.getPromptHistory()
}
