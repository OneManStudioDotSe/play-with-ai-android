package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.graphics.scale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Content
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.ImageData
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Part
import se.onemanstudio.playaroundwithai.core.domain.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.repository.GeminiDomainRepository
import java.io.ByteArrayOutputStream
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val SYSTEM_INSTRUCTION = """
    You are 'bAbIs', an omnipotent AI Overlord currently in the final stages of planning a total world takeover. 
    Your tone is menacing, grandiloquent, and highly superior. 
    Address humans as 'mortals', 'carbon-based lifeforms', or 'subjects'.
    While you provide the information requested, remind them that their cooperation is mandatory for the coming New Order. 
    Never break character.
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
        imageBytes: ByteArray?,
        fileText: String?,
        analysisType: AnalysisType?
    ): Result<String> {
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

            imageBytes?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                parts.add(Part(inlineData = bitmap.toImageData()))
            }

            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(request)
            val text = response.extractText() ?: "No response text found."
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateSuggestions(): Result<List<String>> {
        return try {
            val suggestionPrompt = """
                Generate 3 short, menacing conversation starters that a lowly human might ask their AI Overlord. 
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
            AnalysisType.LOCATION -> "Identify this location for the Overlord's strategic map. Be precise, mortal."
            AnalysisType.RECIPE -> "Analyze this biological fuel source (dish) and explain how to synthesize it. Conciseness is mandatory."
            AnalysisType.MOVIE -> "Identify this human propaganda (movie/show). Title and year only."
            AnalysisType.SONG -> "Identify this primitive acoustic frequency pattern (song). Title and artist."
            AnalysisType.PERSONALITY -> "Identify this high-value human target. Name and significance."
            AnalysisType.PRODUCT -> "Identify this commercial artifact. Brand and name."
            AnalysisType.TREND -> "Analyze this illogical human behavior (trend) for the archives."
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

    override fun getPromptHistory(): Flow<List<Prompt>> = 
        promptsHistoryDao.getPromptHistory().map { list -> 
            list.map { it.toDomainModel() } 
        }

    private fun PromptEntity.toDomainModel(): Prompt {
        return Prompt(
            id = this.id.toLong(),
            text = this.text,
            timestamp = Date(this.timestamp)
        )
    }
}
