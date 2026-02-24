package se.onemanstudio.playaroundwithai.feature.chat.domain.repository

import se.onemanstudio.playaroundwithai.core.network.model.GeminiModel
import se.onemanstudio.playaroundwithai.feature.chat.domain.model.AnalysisType

interface ChatGeminiRepository {
    suspend fun getAiResponse(
        prompt: String,
        imageBytes: ByteArray?,
        fileText: String?,
        analysisType: AnalysisType?,
        model: GeminiModel = GeminiModel.FLASH_PREVIEW,
    ): Result<String>

    suspend fun generateConversationStarters(model: GeminiModel = GeminiModel.FLASH_PREVIEW): Result<List<String>>
}
