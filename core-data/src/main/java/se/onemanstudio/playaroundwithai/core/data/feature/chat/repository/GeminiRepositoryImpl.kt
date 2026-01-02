package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import android.graphics.Bitmap
import android.util.Base64
import androidx.core.graphics.scale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.core.data.AnalysisType
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Content
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiResponse
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.ImageData
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Part
import se.onemanstudio.playaroundwithai.core.data.model.Prompt
import se.onemanstudio.playaroundwithai.core.data.model.mapper.toDomain
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val SYSTEM_INSTRUCTION = """
    You are a fun and slightly sarcastic AI assistant named 'bAbIs'.
    Your goal is to provide helpful, but witty and concise answers.
    Always keep your response playful and never break character.
"""

private const val MAX_SIZE = 768
private const val MAX_SUGGESTIONS = 3
private const val COMPRESSION_QUALITY = 75

@Suppress("MaxLineLength", "TooGenericExceptionCaught")
@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val promptsHistoryDao: PromptsHistoryDao,
) : GeminiRepository {
    override suspend fun generateContent(
        prompt: String,
        imageBitmap: Bitmap?,
        fileText: String?,
        analysisType: AnalysisType?
    ): Result<GeminiResponse> {
        return try {
            val parts = mutableListOf<Part>()
            var fullPrompt = SYSTEM_INSTRUCTION + prompt

            if (analysisType != null) {
                fullPrompt = SYSTEM_INSTRUCTION + "${getSystemInstruction(analysisType)}\n\nUser prompt: $prompt"
            }

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

    override suspend fun generateSuggestions(): Result<List<String>> {
        return try {
            val suggestionPrompt = """
                Generate 3 short, witty, and slightly sarcastic conversation starters that a user could ask you.
                Keep them under 6 words each.
                Format the output strictly as: "Topic 1|Topic 2|Topic 3"
                Do not add any numbering, bullet points, or extra text.
            """.trimIndent()

            val parts = listOf(Part(text = suggestionPrompt))
            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(request)

            val text = response.extractText() ?: ""
            val suggestions = text.split("|").map { it.trim() }.filter { it.isNotEmpty() }

            if (suggestions.isNotEmpty()) {
                Result.success(suggestions.take(MAX_SUGGESTIONS))
            } else {
                Result.failure(Exception("Failed to parse suggestions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        val scaledBitmap = this.scaleBitmap(MAX_SIZE)
        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream)
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

    override suspend fun savePrompt(promptText: String) {
        promptsHistoryDao.insertPrompt(PromptEntity(text = promptText))
    }

    override fun getPromptHistory(): Flow<List<Prompt>> = promptsHistoryDao.getPromptHistory().map { list -> list.map { it.toDomain() } }
}
