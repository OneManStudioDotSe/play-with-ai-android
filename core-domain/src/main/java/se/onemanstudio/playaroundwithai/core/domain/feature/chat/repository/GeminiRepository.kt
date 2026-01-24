package se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace

interface GeminiRepository {
    suspend fun generateContent(prompt: String, imageBytes: ByteArray?, fileText: String?, analysisType: AnalysisType?): Result<String>

    suspend fun generateSuggestions(): Result<List<String>>

    suspend fun getSuggestedPlaces(latitude: Double, longitude: Double): Result<List<SuggestedPlace>>

    suspend fun savePrompt(promptText: String)

    fun getPromptHistory(): Flow<List<Prompt>>

    fun isSyncing(): Flow<Boolean>
}
