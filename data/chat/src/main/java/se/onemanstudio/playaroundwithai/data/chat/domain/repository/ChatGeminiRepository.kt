package se.onemanstudio.playaroundwithai.data.chat.domain.repository

import se.onemanstudio.playaroundwithai.data.chat.domain.model.AnalysisType

interface ChatGeminiRepository {
    suspend fun getAiResponse(
        prompt: String,
        imageBytes: ByteArray?,
        fileText: String?,
        analysisType: AnalysisType?,
    ): Result<String>

    suspend fun generateConversationStarters(): Result<List<String>>
}
