package se.onemanstudio.playaroundwithai.data.chat.domain.usecase

import se.onemanstudio.playaroundwithai.data.chat.domain.repository.ChatGeminiRepository
import javax.inject.Inject

class GetSuggestionsUseCase @Inject constructor(
    private val repository: ChatGeminiRepository
) {
    suspend operator fun invoke(): Result<List<String>> =
        repository.generateConversationStarters()
}
