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
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.ImageData
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.network.prompts.AiPrompts
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.data.chat.domain.model.AnalysisType
import se.onemanstudio.playaroundwithai.data.chat.domain.repository.ChatGeminiRepository
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_IMAGE_SIZE = 768
private const val MAX_SUGGESTIONS = 3
private const val COMPRESSION_QUALITY = 77

@Singleton
class ChatGeminiRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val tokenUsageTracker: TokenUsageTracker,
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
            var fullPrompt = AiPrompts.CHAT_SYSTEM_INSTRUCTION + prompt

            if (analysisType != null) {
                fullPrompt = AiPrompts.CHAT_SYSTEM_INSTRUCTION + "${getAnalysisInstruction(analysisType)}\n\nUser prompt: $prompt"
            }

            if (!fileText.isNullOrBlank()) {
                fullPrompt += "\n\n--- DOCUMENT CONTEXT ---\n$fileText"
                Timber.d("Appended document context (${fileText.length} chars)")
            }

            parts.add(Part(text = fullPrompt))

            imageBytes?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                Timber.d("Gemini - Encoding image: ${bitmap.width}x${bitmap.height} → scaled to max ${MAX_IMAGE_SIZE}px, JPEG @ $COMPRESSION_QUALITY%%")
                parts.add(Part(inlineData = bitmap.toImageData()))
            }

            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            Timber.d("Gemini - Sending request to Gemini API with ${parts.size} parts...")
            val response = apiService.generateContent(request)
            tokenUsageTracker.record("chat", response.usageMetadata)
            val text = response.extractText() ?: "No response text found."
            Timber.d("Gemini - API response received (and it is ${text.length} chars)")
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

            val suggestionPrompt = AiPrompts.CONVERSATION_STARTERS_PROMPT

            val parts = listOf(Part(text = suggestionPrompt))
            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(request)
            tokenUsageTracker.record("chat", response.usageMetadata)

            val text = response.extractText() ?: ""
            val suggestions = text.split("|").map { it.trim() }.filter { it.isNotEmpty() }

            if (suggestions.isNotEmpty()) {
                Timber.d("Generated ${suggestions.size} suggestion(s): $suggestions")
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
        }
    }

    private fun getAnalysisInstruction(analysisType: AnalysisType): String {
        return AiPrompts.ANALYSIS_INSTRUCTIONS[analysisType.name].orEmpty()
    }

    private fun Bitmap.toImageData(): ImageData {
        val scaledBitmap = this.scaleBitmap(MAX_IMAGE_SIZE)
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
}
