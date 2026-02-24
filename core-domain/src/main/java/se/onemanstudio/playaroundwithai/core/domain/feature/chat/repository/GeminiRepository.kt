package se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.GeminiModel
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace

interface GeminiRepository {
    suspend fun getAiResponse(
        prompt: String,
        imageBytes: ByteArray?,
        fileText: String?,
        analysisType: AnalysisType?,
        model: GeminiModel = GeminiModel.FLASH_PREVIEW,
    ): Result<String>

    suspend fun generateConversationStarters(model: GeminiModel = GeminiModel.FLASH_PREVIEW): Result<List<String>>

    suspend fun getSuggestedPlaces(
        latitude: Double,
        longitude: Double,
        model: GeminiModel = GeminiModel.FLASH_PREVIEW,
    ): Result<List<SuggestedPlace>>
}
