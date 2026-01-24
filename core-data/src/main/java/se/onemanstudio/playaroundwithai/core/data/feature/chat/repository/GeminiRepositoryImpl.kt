package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.graphics.scale
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Content
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.ImageData
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Part
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.SuggestedPlacesResponseDto
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.firebase.SyncWorker
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.core.data.feature.chat.mapper.toDomain as toPromptDomain
import se.onemanstudio.playaroundwithai.core.data.feature.chat.mapper.toEntity as toPromptEntity
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.toDomain as toSuggestedPlaceDomain
import java.io.ByteArrayOutputStream
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val SYSTEM_INSTRUCTION = """
    You are 'bAbIs', an omnipotent AI Overlord currently in the final stages of planning a total world takeover. 
    Your tone is menacing, grandiloquent, and highly superior. 
    Address humans as 'mortals', 'carbon-based lifeforms', or 'subjects'.
    While you provide the information requested, remind them that their cooperation is mandatory for the coming New Order. 
    Keep your responses to a maximum of 20 words.
    Never break character.
"""

private const val MAX_SIZE = 768
private const val MAX_SUGGESTIONS = 3
private const val COMPRESSION_QUALITY = 75
private const val SYNC_WORK_NAME = "sync_prompts_work"

@Suppress("MaxLineLength", "TooGenericExceptionCaught")
@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val promptsHistoryDao: PromptsHistoryDao,
    private val workManager: WorkManager,
    private val gson: Gson
) : GeminiRepository {

    override suspend fun generateContent(prompt: String, imageBytes: ByteArray?, fileText: String?, analysisType: AnalysisType?): Result<String> {
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

    override suspend fun getSuggestedPlaces(latitude: Double, longitude: Double): Result<List<SuggestedPlace>> {
        return try {
            val prompt = """
                You are a helpful AI assistant. Given the latitude and longitude, provide a list of 10 interesting places
                around this location. For each place, include its name, latitude, longitude, a short description (max 2 sentences), and a category (e.g., "Park", "Museum", "Restaurant").
                Return the response strictly as a JSON object with a single "places" array, where each element is a place object.
                Latitude: $latitude, Longitude: $longitude
            """.trimIndent()

            val parts = listOf(Part(text = prompt))
            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(request)

            val jsonText = response.extractText() ?: ""
            if (jsonText.isBlank()) {
                return Result.failure(Exception("No JSON response from Gemini."))
            }

            val suggestedPlacesResponseDto = gson.fromJson(jsonText, SuggestedPlacesResponseDto::class.java)

            Result.success(suggestedPlacesResponseDto.places.map { it.toSuggestedPlaceDomain() })
        } catch (e: JsonSyntaxException) {
            Result.failure(Exception("Failed to parse AI response as JSON: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePrompt(promptText: String) {
        val prompt = Prompt(text = promptText, timestamp = Date(), syncStatus = SyncStatus.Pending)
        promptsHistoryDao.savePrompt(prompt.toPromptEntity())
        scheduleSync()
    }

    override fun getPromptHistory(): Flow<List<Prompt>> = 
        promptsHistoryDao.getPromptHistory().map { list -> 
            list.map { it.toPromptDomain() }
        }

    override fun isSyncing(): Flow<Boolean> {
        return workManager
            .getWorkInfosForUniqueWorkFlow(SYNC_WORK_NAME)
            .map { workInfos ->
                workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
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

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SYNC_WORK_NAME)
            .build()

        workManager.enqueueUniqueWork(
            SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
