package se.onemanstudio.playaroundwithai.data.explore.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import se.onemanstudio.playaroundwithai.data.explore.data.dto.SuggestedPlacesResponseDto
import se.onemanstudio.playaroundwithai.data.explore.data.dto.toSuggestedPlaceDomain
import se.onemanstudio.playaroundwithai.data.explore.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.data.explore.domain.repository.ExploreSuggestionsRepository
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.network.prompts.AiPrompts
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreSuggestionsRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val gson: Gson,
    private val tokenUsageTracker: TokenUsageTracker,
) : ExploreSuggestionsRepository {

    override suspend fun getSuggestedPlaces(
        latitude: Double,
        longitude: Double,
    ): Result<List<SuggestedPlace>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Gemini - Getting suggested places for lat=$latitude, lng=$longitude")

            val prompt = AiPrompts.suggestedPlacesPrompt(latitude, longitude)
            val parts = listOf(Part(text = prompt))
            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(request)
            tokenUsageTracker.record("explore", response.usageMetadata)

            val rawText = response.extractText() ?: ""
            if (rawText.isBlank()) {
                return@withContext Result.failure(IOException("No JSON response from Gemini."))
            }

            val jsonText = extractJson(rawText)
            val dto = gson.fromJson(jsonText, SuggestedPlacesResponseDto::class.java)

            Timber.d("Gemini - Received ${dto.places.size} suggested places")
            Result.success(dto.places.map { it.toSuggestedPlaceDomain() })
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "Gemini - Failed to parse AI response as JSON")
            Result.failure(e)
        } catch (e: IOException) {
            Timber.e(e, "Gemini - Network error during getSuggestedPlaces")
            Result.failure(e)
        } catch (e: HttpException) {
            Timber.e(e, "Gemini - HTTP error during getSuggestedPlaces (code=${e.code()})")
            Result.failure(e)
        }
    }

    private fun extractJson(text: String): String {
        val codeFencePattern = Regex("""```\w*\s*([\s\S]*?)```""")
        val match = codeFencePattern.find(text)
        return (match?.groupValues?.getOrNull(1) ?: text).trim()
    }
}
