package se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType

interface GeminiRepository {
    suspend fun generateContent(prompt: String, imageBytes: ByteArray?, fileText: String?, analysisType: AnalysisType?): Result<String>

    suspend fun generateConversationStarters(): Result<List<String>>
}