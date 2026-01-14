package se.onemanstudio.playaroundwithai.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.domain.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.repository.GeminiDomainRepository
import javax.inject.Inject

class GetPromptHistoryUseCase @Inject constructor(
    private val repository: GeminiDomainRepository
) {
    operator fun invoke(): Flow<List<Prompt>> {
        return repository.getPromptHistory()
    }
}
