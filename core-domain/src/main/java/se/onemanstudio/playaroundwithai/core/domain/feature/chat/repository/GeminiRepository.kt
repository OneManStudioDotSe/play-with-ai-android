package se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository

import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace

interface GeminiRepository {
    suspend fun generateContent(prompt: String, imageBytes: ByteArray?, fileText: String?, analysisType: AnalysisType?): Result<String>

    suspend fun generateConversationStarters(): Result<List<String>>

    suspend fun getSuggestedPlaces(latitude: Double, longitude: Double): Result<List<SuggestedPlace>>
}
