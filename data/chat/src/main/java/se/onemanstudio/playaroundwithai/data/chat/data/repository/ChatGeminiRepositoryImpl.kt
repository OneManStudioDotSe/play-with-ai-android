package se.onemanstudio.playaroundwithai.data.chat.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.graphics.scale
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import se.onemanstudio.playaroundwithai.core.config.settings.AppSettingsHolder
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.ImageData
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.tracking.repository.TokenUsageTracker
import se.onemanstudio.playaroundwithai.data.chat.prompts.ChatPrompts
import se.onemanstudio.playaroundwithai.data.chat.domain.model.AnalysisType
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.ChatGeminiRepository
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_SUGGESTIONS = 3

private const val IMAGE_QUALITY_LOW = 40
private const val IMAGE_QUALITY_HIGH = 93
private const val IMAGE_SIZE_LOW = 512
private const val IMAGE_SIZE_MEDIUM = 768
private const val IMAGE_SIZE_HIGH = 1024

private fun imageSizeForQuality(quality: Int): Int = when (quality) {
    IMAGE_QUALITY_LOW -> IMAGE_SIZE_LOW
    IMAGE_QUALITY_HIGH -> IMAGE_SIZE_HIGH
    else -> IMAGE_SIZE_MEDIUM
}

@Singleton
class ChatGeminiRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val tokenUsageTracker: TokenUsageTracker,
    private val appSettingsHolder: AppSettingsHolder,
) : ChatGeminiRepository {

    override suspend fun getAiResponse(
        prompt: String,
        imageBytes: ByteArray?,
        fileText: String?,
        analysisType: AnalysisType?,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Gemini - Generating content for a prompt with length ${prompt.length} characters, hasImage: " +
                    "${imageBytes != null}, hasFile: ${fileText != null} and analysisType: $analysisType")

            val parts = mutableListOf<Part>()
            var fullPrompt = ChatPrompts.CHAT_SYSTEM_INSTRUCTION + prompt

            if (analysisType != null) {
                fullPrompt = ChatPrompts.CHAT_SYSTEM_INSTRUCTION + "${getAnalysisInstruction(analysisType)}\n\nUser prompt: $prompt"
            }

            if (!fileText.isNullOrBlank()) {
                fullPrompt += "\n\n--- DOCUMENT CONTEXT ---\n$fileText"
            }

            parts.add(Part(text = fullPrompt))

            imageBytes?.let {
                val inlineData = withContext(Dispatchers.Default) {
                    val quality = appSettingsHolder.imageQualityJpeg.value
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    bitmap.toImageData(quality, imageSizeForQuality(quality))
                }
                parts.add(Part(inlineData = inlineData))
            }

            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(appSettingsHolder.geminiTextModel.value, request)
            tokenUsageTracker.record("chat", response.usageMetadata)
            val text = response.extractText() ?: "No response text found."

            Result.success(text)
        } catch (e: IOException) {
            Timber.e(e, "Gemini - Network error during content generation")
            Result.failure(e)
        } catch (e: HttpException) {
            Timber.e(e, "Gemini - HTTP error during content generation (code=${e.code()})")
            Result.failure(e)
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "Gemini - Failed to parse API response")
            Result.failure(e)
        }
    }

    override suspend fun generateConversationStarters(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Gemini - Generating conversation starters from API...")

            val suggestionPrompt = ChatPrompts.CONVERSATION_STARTERS_PROMPT

            val parts = listOf(Part(text = suggestionPrompt))
            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(appSettingsHolder.geminiTextModel.value, request)
            tokenUsageTracker.record("chat", response.usageMetadata)

            val text = response.extractText() ?: ""
            val suggestions = text.split("|").map { it.trim() }.filter { it.isNotEmpty() }

            if (suggestions.isNotEmpty()) {
                Result.success(suggestions.take(MAX_SUGGESTIONS))
            } else {
                Timber.w("Failed to parse suggestions from response: '$text'")
                Result.failure(Exception("Failed to parse suggestions"))
            }
        } catch (e: IOException) {
            Timber.e(e, "Gemini - Network error during conversation starters")
            Result.failure(e)
        } catch (e: HttpException) {
            Timber.e(e, "Gemini - HTTP error during conversation starters (code=${e.code()})")
            Result.failure(e)
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "Gemini - Failed to parse conversation starters response")
            Result.failure(e)
        }
    }

    private fun getAnalysisInstruction(analysisType: AnalysisType): String {
        val instruction = ChatPrompts.ANALYSIS_INSTRUCTIONS[analysisType.name].orEmpty()
        if (instruction.isEmpty()) Timber.w("ChatGemini - No analysis instruction found for type ${analysisType.name}")
        return instruction
    }

    private fun Bitmap.toImageData(quality: Int, maxDimension: Int): ImageData {
        val scaledBitmap = this.scaleBitmap(maxDimension)
        val byteArrayOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
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
}
