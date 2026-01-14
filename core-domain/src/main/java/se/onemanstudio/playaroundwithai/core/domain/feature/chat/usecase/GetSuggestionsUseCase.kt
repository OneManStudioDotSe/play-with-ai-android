package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import javax.inject.Inject

class GetSuggestionsUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return repository.generateSuggestions()
    }
}
