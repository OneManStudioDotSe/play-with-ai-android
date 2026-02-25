package se.onemanstudio.playaroundwithai.data.dream.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamInterpretation
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamGeminiRepository
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DreamGeminiRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val gson: Gson,
    private val tokenUsageTracker: TokenUsageTracker,
) : DreamGeminiRepository {

    @Suppress("TooGenericExceptionCaught")
    override suspend fun interpretDream(description: String): Result<DreamInterpretation> = withContext(Dispatchers.IO) {
        try {
            Timber.d("DreamGemini - Interpreting dream...")

            val prompt = buildPrompt(description)
            val parts = listOf(Part(text = prompt))
            val request = GeminiRequest(contents = listOf(Content(parts = parts)))

            Timber.d("DreamGemini - Sending request to Gemini API...")
            val response = apiService.generateContent(request)
            tokenUsageTracker.record("dream", response.usageMetadata)
            val text = response.extractText() ?: return@withContext Result.failure(Exception("No response text from Gemini"))
            Timber.d("DreamGemini - API response received (${text.length} chars)")

            val cleanedJson = cleanJsonResponse(text)
            val interpretation = parseInterpretation(cleanedJson)
            Result.success(interpretation)
        } catch (e: IOException) {
            Timber.e(e, "DreamGemini - Network error")
            Result.failure(e)
        } catch (e: HttpException) {
            Timber.e(e, "DreamGemini - HTTP error (code=${e.code()})")
            Result.failure(e)
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "DreamGemini - Failed to parse API response")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "DreamGemini - Unexpected error")
            Result.failure(e)
        }
    }

    private fun parseInterpretation(json: String): DreamInterpretation {
        val parsed = gson.fromJson(json, GeminiDreamResponse::class.java)

        val mood = runCatching { DreamMood.valueOf(parsed.mood.uppercase()) }.getOrDefault(DreamMood.MYSTERIOUS)

        val scene = DreamScene(
            palette = parsed.scene.palette.toDomain(),
            layers = parsed.scene.layers.map { it.toDomain() },
            particles = parsed.scene.particles.map { it.toDomain() },
        )

        return DreamInterpretation(
            textAnalysis = parsed.interpretation,
            scene = scene,
            mood = mood,
        )
    }

    private fun cleanJsonResponse(text: String): String =
        text.trim()
            .removeSurrounding("```json", "```")
            .removeSurrounding("```")
            .trim()

    companion object {
        @Suppress("MaxLineLength")
        private fun buildPrompt(description: String): String = """
You are a dream interpreter and visual artist. Given the user's dream description below, return a JSON object with exactly this structure:
{
  "interpretation": "A 2-3 sentence analysis of symbolism, emotional meaning, and themes",
  "mood": "one of: JOYFUL, MYSTERIOUS, ANXIOUS, PEACEFUL, DARK, SURREAL",
  "scene": {
    "palette": { "sky": <ARGB long>, "horizon": <ARGB long>, "accent": <ARGB long> },
    "layers": [
      {
        "depth": <0.0-1.0>,
        "elements": [{ "shape": "<CIRCLE|TRIANGLE|MOUNTAIN|WAVE|TREE|CLOUD|STAR|CRESCENT|DIAMOND|SPIRAL|LOTUS|AURORA|CRYSTAL>", "x": <0.0-1.0>, "y": <0.0-1.0>, "scale": <0.5-3.0>, "color": <ARGB long>, "alpha": <0.0-1.0> }]
      }
    ],
    "particles": [{ "shape": "<DOT|SPARKLE|RING|TEARDROP|DIAMOND_MOTE|DASH|STARBURST>", "count": <5-30>, "color": <ARGB long>, "speed": <0.5-2.0>, "size": <2.0-8.0> }]
  }
}
Generate 3-5 layers with 2-4 elements each. Use colors that match the dream mood. ARGB long values should be like 4278190335 (0xFF0000FF for blue).
Shape guidance:
- Nature: TREE, MOUNTAIN, LOTUS, AURORA, WAVE, CLOUD. Particles: TEARDROP, DOT
- Night/space: STAR, CRESCENT, CRYSTAL, CIRCLE. Particles: SPARKLE, STARBURST, DIAMOND_MOTE
- Abstract/surreal: SPIRAL, DIAMOND, AURORA, WAVE. Particles: RING, DASH, DIAMOND_MOTE
- Water/ocean: WAVE, CIRCLE, CRESCENT. Particles: TEARDROP, DOT, RING
Use diverse shapes across layers. Mix 3-5 different element shapes and 2-3 particle types per scene.
Return ONLY valid JSON, no markdown, no backticks, no extra text.

Dream: "$description"
        """.trimIndent()
    }
}

// Internal DTOs for Gson parsing of the Gemini JSON response
private data class GeminiDreamResponse(
    val interpretation: String = "",
    val mood: String = "MYSTERIOUS",
    val scene: GeminiSceneDto = GeminiSceneDto(),
)

private data class GeminiSceneDto(
    val palette: GeminiPaletteDto = GeminiPaletteDto(),
    val layers: List<GeminiLayerDto> = emptyList(),
    val particles: List<GeminiParticleDto> = emptyList(),
)

private data class GeminiPaletteDto(
    val sky: Long = 0xFF1A1A2E,
    val horizon: Long = 0xFF16213E,
    val accent: Long = 0xFF0F3460,
) {
    fun toDomain() = se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette(sky = sky, horizon = horizon, accent = accent)
}

private data class GeminiLayerDto(
    val depth: Float = 0.5f,
    val elements: List<GeminiElementDto> = emptyList(),
) {
    fun toDomain() = se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamLayer(
        depth = depth,
        elements = elements.map { it.toDomain() },
    )
}

@Suppress("MagicNumber")
private data class GeminiElementDto(
    val shape: String = "CIRCLE",
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val scale: Float = 1.0f,
    val color: Long = 0xFFFFFFFF,
    val alpha: Float = 1.0f,
) {
    fun toDomain() = se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamElement(
        shape = runCatching {
            se.onemanstudio.playaroundwithai.data.dream.domain.model.ElementShape.valueOf(shape.uppercase())
        }.getOrDefault(se.onemanstudio.playaroundwithai.data.dream.domain.model.ElementShape.CIRCLE),
        x = x, y = y, scale = scale, color = color, alpha = alpha,
    )
}

@Suppress("MagicNumber")
private data class GeminiParticleDto(
    val shape: String = "DOT",
    val count: Int = 10,
    val color: Long = 0xFFFFFFFF,
    val speed: Float = 1.0f,
    val size: Float = 4.0f,
) {
    fun toDomain() = se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamParticle(
        shape = runCatching {
            se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape.valueOf(shape.uppercase())
        }.getOrDefault(se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape.DOT),
        count = count, color = color, speed = speed, size = size,
    )
}
