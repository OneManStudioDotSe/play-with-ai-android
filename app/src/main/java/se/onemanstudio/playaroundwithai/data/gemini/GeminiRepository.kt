package se.onemanstudio.playaroundwithai.data.gemini

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.BuildConfig
import se.onemanstudio.playaroundwithai.data.gemini.network.GeminiApiService
import se.onemanstudio.playaroundwithai.data.local.PromptDao
import se.onemanstudio.playaroundwithai.data.local.PromptEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    private val apiService: GeminiApiService,
    private val promptDao: PromptDao // Inject the DAO
) {
    suspend fun generateContent(prompt: String): Result<GeminiResponse> {
        return try {
            // Request 3: Modify the prompt to ask for a concise response
            val modifiedPrompt = "Your response must be concise and under 50 words. Do not use markdown. User prompt: $prompt"

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = modifiedPrompt))))
            )
            val response = apiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // New function to save a prompt
    suspend fun savePrompt(promptText: String) {
        promptDao.insertPrompt(PromptEntity(text = promptText))
    }

    // New function to get the history
    fun getPromptHistory(): Flow<List<PromptEntity>> = promptDao.getPromptHistory()
}
