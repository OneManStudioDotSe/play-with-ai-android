package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import javax.inject.Inject

class GetPromptHistoryUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    operator fun invoke(): Flow<List<Prompt>> {
        return repository.getPromptHistory()
    }
}
