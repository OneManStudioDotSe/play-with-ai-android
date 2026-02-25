package se.onemanstudio.playaroundwithai.data.maps.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import se.onemanstudio.playaroundwithai.data.maps.data.dto.SuggestedPlacesResponseDto
import se.onemanstudio.playaroundwithai.data.maps.data.dto.toSuggestedPlaceDomain
import se.onemanstudio.playaroundwithai.data.maps.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.data.maps.domain.repository.MapSuggestionsRepository
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val SUGGESTED_PLACES_COUNT = 10

@Singleton
class MapSuggestionsRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val gson: Gson,
    private val tokenUsageTracker: TokenUsageTracker,
) : MapSuggestionsRepository {

    override suspend fun getSuggestedPlaces(
        latitude: Double,
        longitude: Double,
    ): Result<List<SuggestedPlace>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Gemini - Getting suggested places for lat=$latitude, lng=$longitude")

            val prompt = buildSuggestedPlacesPrompt(latitude, longitude)
            val parts = listOf(Part(text = prompt))
            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(request)
            tokenUsageTracker.record("map", response.usageMetadata)

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

    private fun buildSuggestedPlacesPrompt(latitude: Double, longitude: Double): String {
        return """
            You are a helpful AI assistant. Given the latitude and longitude,
            provide a list of $SUGGESTED_PLACES_COUNT interesting places around this location.
            For each place, include its name, latitude, longitude,
            a short description (max 2 sentences),
            and a category (e.g., "Park", "Museum", "Restaurant").
            Return the response strictly as a JSON object with a single "places" array,
            where each element is a place object.
            Latitude: $latitude, Longitude: $longitude
        """.trimIndent()
    }

    private fun extractJson(text: String): String {
        val codeFencePattern = Regex("""```\w*\s*([\s\S]*?)```""")
        val match = codeFencePattern.find(text)
        return (match?.groupValues?.get(1) ?: text).trim()
    }
}
