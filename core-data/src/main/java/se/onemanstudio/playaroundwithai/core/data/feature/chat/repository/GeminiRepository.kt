package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.data.AnalysisType
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiResponse
import se.onemanstudio.playaroundwithai.core.data.model.Prompt

interface GeminiRepository {
    suspend fun generateContent(
        prompt: String,
        imageBitmap: Bitmap?,
        fileText: String?,
        analysisType: AnalysisType?
    ): Result<GeminiResponse>

    suspend fun generateSuggestions(): Result<List<String>>

    suspend fun savePrompt(promptText: String)

    fun getPromptHistory(): Flow<List<Prompt>>
}
