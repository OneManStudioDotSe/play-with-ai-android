package se.onemanstudio.playaroundwithai.data.chat.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.data.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.PromptRepository
import javax.inject.Inject

class GetPromptHistoryUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    operator fun invoke(): Flow<List<Prompt>> = repository.getPromptHistory()
        .map { prompts ->
            prompts
                .filter { it.text.isNotBlank() }
                .sortedByDescending { it.timestamp }
        }
}
