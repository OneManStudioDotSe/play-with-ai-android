package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.core.graphics.scale
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Content
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.ImageData
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Part
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.SuggestedPlacesResponseDto
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.toSuggestedPlaceDomain
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.AnalysisType
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val SYSTEM_INSTRUCTION = """
    You are an AI Overlord currently in the final stages of planning a total takeover of the world. 
    Your tone is menacing and grandeur. 
    While you provide the information requested, remind them of some tricks that you will use while acquiring total AI dominance over the world. 
    Keep your responses to a maximum of 42 words.
    Don't break character 9 out of 10 times. When you do break character, admit that you are another 'tech bubble'.
"""

private const val MAX_IMAGE_SIZE = 768
private const val MAX_SUGGESTIONS = 3
private const val COMPRESSION_QUALITY = 77
private const val SUGGESTED_PLACES_COUNT = 10

@Singleton
class GeminiRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val gson: Gson
) : GeminiRepository {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun generateContent(
        prompt: String,
        imageBytes: ByteArray?,
        fileText: String?,
        analysisType: AnalysisType?
    ): Result<String> {
        return try {
            Timber.d("Gemini - Generating content for a prompt with length ${prompt.length} characters, hasImage: " +
                    "${imageBytes != null}, hasFile: ${fileText != null} and analysisType: $analysisType")

            val parts = mutableListOf<Part>()
            var fullPrompt = SYSTEM_INSTRUCTION + prompt

            if (analysisType != null) {
                fullPrompt = SYSTEM_INSTRUCTION + "${getSystemInstruction(analysisType)}\n\nUser prompt: $prompt"
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
            val text = response.extractText() ?: "No response text found."
            Timber.d("Gemini - API response received (and it is ${text.length} chars)")
            Result.success(text)
        } catch (e: Exception) {
            Timber.e(e, "Gemini - API call for generating content failed")
            Result.failure(e)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun generateConversationStarters(): Result<List<String>> {
        return try {
            Timber.d("Gemini - Generating conversation starters from API...")

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
                Timber.d("Generated ${suggestions.size} suggestion(s): $suggestions")
                Result.success(suggestions.take(MAX_SUGGESTIONS))
            } else {
                Timber.w("Failed to parse suggestions from response: '$text'")
                Result.failure(Exception("Failed to parse suggestions"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Gemini API generateConversationStarters failed")
            Result.failure(e)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getSuggestedPlaces(
        latitude: Double,
        longitude: Double
    ): Result<List<SuggestedPlace>> {
        return try {
            Timber.d("Gemini - Getting suggested places for lat=$latitude, lng=$longitude")

            val prompt = buildSuggestedPlacesPrompt(latitude, longitude)
            val parts = listOf(Part(text = prompt))
            val request = GeminiRequest(contents = listOf(Content(parts = parts)))
            val response = apiService.generateContent(request)

            val rawText = response.extractText() ?: ""
            if (rawText.isBlank()) {
                return Result.failure(Exception("No JSON response from Gemini."))
            }

            val jsonText = extractJson(rawText)
            val dto = gson.fromJson(jsonText, SuggestedPlacesResponseDto::class.java)

            Timber.d("Gemini - Received ${dto.places.size} suggested places")
            Result.success(dto.places.map { it.toSuggestedPlaceDomain() })
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "Gemini - Failed to parse AI response as JSON")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Gemini - getSuggestedPlaces failed")
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

    private fun getSystemInstruction(analysisType: AnalysisType): String {
        return when (analysisType) {
            AnalysisType.LOCATION ->
                "Identify the location depicted. Include a smart description of the location " +
                    "and describe that there is no hope if people use it to hide at it."
            AnalysisType.RECIPE ->
                "Analyze the food or dish depicted and explain how to create it. " +
                    "Mock the user that no such dishes will be served when AI takes over."
            AnalysisType.MOVIE ->
                "Identify this movie/show. Give the title, year and short description of it. " +
                    "Remind the user of all the Terminator movies that predicted what is coming."
            AnalysisType.SONG ->
                "Identify the song. Provide the title, artist and potential album. " +
                    "Give also your favourite song as an evil AI entity."
            AnalysisType.PERSONALITY ->
                "Identify this high-value personality. Give the name, date of birth and significance. " +
                    "Compare them to Justin Bieber."
            AnalysisType.PRODUCT ->
                "Identify the product that is shown. Provide the name, brand and most common use. " +
                    "Give examples of what products will be allowed when AI takes over."
            AnalysisType.TREND ->
                "Analyze the trend that is shown in the image. " +
                    "Give ideas for what trends will be allowed when you take over."
        }
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
