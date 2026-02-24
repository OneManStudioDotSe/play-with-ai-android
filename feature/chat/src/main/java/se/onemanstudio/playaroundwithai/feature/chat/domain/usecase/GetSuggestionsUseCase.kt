package se.onemanstudio.playaroundwithai.feature.chat.domain.usecase

import se.onemanstudio.playaroundwithai.core.network.model.GeminiModel
import se.onemanstudio.playaroundwithai.feature.chat.domain.repository.ChatGeminiRepository
import javax.inject.Inject

class GetSuggestionsUseCase @Inject constructor(
    private val repository: ChatGeminiRepository
) {
    private var cachedSuggestions: List<String>? = null

    suspend operator fun invoke(model: GeminiModel = GeminiModel.FLASH_PREVIEW): Result<List<String>> {
        val result = repository.generateConversationStarters(model)

        result.onSuccess { suggestions ->
            cachedSuggestions = suggestions
        }

        if (result.isFailure) {
            cachedSuggestions?.let { cached ->
                return Result.success(cached)
            }
        }

        return result
    }
}
