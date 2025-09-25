package se.onemanstudio.playaroundwithai.data.gemini

import se.onemanstudio.playaroundwithai.BuildConfig
import se.onemanstudio.playaroundwithai.data.gemini.network.GeminiApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    private val apiService: GeminiApiService
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
}
