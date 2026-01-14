package se.onemanstudio.playaroundwithai.core.domain.repository

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.domain.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.model.Prompt

interface GeminiDomainRepository {
    suspend fun generateContent(
        prompt: String,
        imageBytes: ByteArray?,
        fileText: String?,
        analysisType: AnalysisType?
    ): Result<String>

    suspend fun generateSuggestions(): Result<List<String>>

    suspend fun savePrompt(promptText: String)

    fun getPromptHistory(): Flow<List<Prompt>>
}
