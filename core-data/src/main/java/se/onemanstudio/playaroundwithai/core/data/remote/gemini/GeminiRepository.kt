package se.onemanstudio.playaroundwithai.core.data.remote.gemini

import android.graphics.Bitmap
import android.util.Base64
import androidx.core.graphics.scale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.core.data.AnalysisType
import se.onemanstudio.playaroundwithai.core.data.domain.mapper.toDomain
import se.onemanstudio.playaroundwithai.core.data.domain.model.Prompt
import se.onemanstudio.playaroundwithai.core.data.local.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.local.PromptEntity
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.model.Content
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.model.GeminiRequest
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.model.GeminiResponse
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.model.ImageData
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.model.Part
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.network.GeminiApiService
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val SYSTEM_INSTRUCTION = """
    You are a fun and slightly sarcastic AI assistant named 'bAbIs'.
    Your goal is to provide helpful, but witty and concise answers.
    Always keep your response playful and never break character.
"""

private const val MAX_SIZE = 768

private const val COMPRESSION_QUALITY = 75

@Suppress("MaxLineLength", "TooGenericExceptionCaught")
@Singleton
class GeminiRepository @Inject constructor(
    private val apiService: GeminiApiService,
    private val promptsHistoryDao: PromptsHistoryDao,
) {
    suspend fun generateContent(
        prompt: String,
        imageBitmap: Bitmap?,
        fileText: String?,
        analysisType: AnalysisType?
    ): Result<GeminiResponse> {
        return try {
            val parts = mutableListOf<Part>()
            var fullPrompt = SYSTEM_INSTRUCTION + prompt

            // Add system instruction if it's an image analysis
            if (analysisType != null) {
                fullPrompt = SYSTEM_INSTRUCTION + "${getSystemInstruction(analysisType)}\n\nUser prompt: $prompt"
            }

            // Append document text if it exists
            if (!fileText.isNullOrBlank()) {
                fullPrompt += "\n\n--- DOCUMENT CONTEXT ---\n$fileText"
            }

            parts.add(Part(text = fullPrompt))

            imageBitmap?.let {
                parts.add(Part(inlineData = it.toImageData()))
            }

            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // This function provides the correct context based on the dropdown selection
    private fun getSystemInstruction(analysisType: AnalysisType): String {
        return when (analysisType) {
            AnalysisType.LOCATION -> "You are an expert location identifier. Analyze the attached screenshot(s) to identify the geographical location (city, landmark, country). Be specific and concise."
            AnalysisType.RECIPE -> "You are a culinary expert. Analyze the attached screenshot(s) to identify the dish and provide a simple recipe for it. Be concise."
            AnalysisType.MOVIE -> "You are a film expert. Analyze the attached screenshot(s) to identify the movie or TV show. Provide the title and year. Be concise."
            AnalysisType.SONG -> "You are a music expert. Analyze the attached screenshot(s) to identify the song mentioned or displayed. Provide the song title and artist. Be concise."
            AnalysisType.PERSONALITY -> "You are a pop culture expert. Analyze the attached screenshot(s) to identify the famous personality (actor, musician, influencer). Provide their name and what they are known for. Be concise."
            AnalysisType.PRODUCT -> "You are a product identification specialist. Analyze the attached screenshot(s) to identify the commercial product shown. Provide the brand and product name. Be concise."
            AnalysisType.TREND -> "You are a social media trend analyst. Analyze the attached screenshot(s) to identify the TikTok trend being shown. Describe the trend briefly and concisely."
        }
    }

    private fun Bitmap.toImageData(): ImageData {
        val scaledBitmap = this.scaleBitmap(MAX_SIZE) // Max dimension of 768px

        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream) // 75% quality
        val byteArray = byteArrayOutputStream.toByteArray()

        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        return ImageData(mimeType = "image/jpeg", data = base64String)
    }

    private fun Bitmap.scaleBitmap(maxDimension: Int): Bitmap {
        val originalWidth = this.width
        val originalHeight = this.height
        var resizedWidth = maxDimension
        var resizedHeight = maxDimension

        if (originalHeight > originalWidth) {
            resizedWidth = (resizedHeight * originalWidth.toFloat() / originalHeight.toFloat()).toInt()
        } else if (originalWidth > originalHeight) {
            resizedHeight = (resizedWidth * originalHeight.toFloat() / originalWidth.toFloat()).toInt()
        }

        return this.scale(resizedWidth, resizedHeight, false)
    }

    suspend fun savePrompt(promptText: String) {
        promptsHistoryDao.insertPrompt(PromptEntity(text = promptText))
    }

    fun getPromptHistory(): Flow<List<Prompt>> = promptsHistoryDao.getPromptHistory().map { list -> list.map { it.toDomain() } }
}
