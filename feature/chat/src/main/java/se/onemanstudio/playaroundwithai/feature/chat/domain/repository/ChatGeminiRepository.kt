package se.onemanstudio.playaroundwithai.feature.chat.domain.repository

import se.onemanstudio.playaroundwithai.feature.chat.domain.model.AnalysisType

interface ChatGeminiRepository {
    suspend fun getAiResponse(
        prompt: String,
        imageBytes: ByteArray?,
        fileText: String?,
        analysisType: AnalysisType?,
    ): Result<String>

    suspend fun generateConversationStarters(): Result<List<String>>
}
