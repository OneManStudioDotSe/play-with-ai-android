package se.onemanstudio.playaroundwithai.core.domain.usecase

import se.onemanstudio.playaroundwithai.core.domain.repository.GeminiDomainRepository
import javax.inject.Inject

class GetSuggestionsUseCase @Inject constructor(
    private val repository: GeminiDomainRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return repository.generateSuggestions()
    }
}
