package se.onemanstudio.playaroundwithai.data.dream.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.GenerationConfig
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.data.dream.prompts.DreamPrompts
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamImage
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

            val prompt = DreamPrompts.interpretationPrompt(description)
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

    @Suppress("TooGenericExceptionCaught")
    override suspend fun generateDreamImage(description: String): Result<DreamImage> = withContext(Dispatchers.IO) {
        try {
            Timber.d("DreamGemini - Generating dream image...")

            val prompt = DreamPrompts.imagePrompt(description)
            val parts = listOf(Part(text = prompt))
            val request = GeminiRequest(
                contents = listOf(Content(parts = parts)),
                generationConfig = GenerationConfig(responseModalities = listOf("IMAGE", "TEXT")),
            )

            var imageData: se.onemanstudio.playaroundwithai.core.network.dto.ImageData? = null
            var lastText = ""

            for (attempt in 1..IMAGE_GENERATION_MAX_RETRIES) {
                Timber.d("DreamGemini - Image generation attempt %d/%d...", attempt, IMAGE_GENERATION_MAX_RETRIES)
                val response = apiService.generateImageContent(request)
                tokenUsageTracker.record("dream_image", response.usageMetadata)

                imageData = response.extractImageData()
                lastText = response.extractText().orEmpty()

                if (imageData != null) {
                    Timber.d("DreamGemini - Image data received on attempt %d", attempt)
                    break
                }
                Timber.w("DreamGemini - Attempt %d returned text only: %s", attempt, lastText.take(RETRY_LOG_PREVIEW_LENGTH))
            }

            if (imageData == null) {
                return@withContext Result.failure(Exception("No image data after $IMAGE_GENERATION_MAX_RETRIES attempts"))
            }

            val artistName = ARTIST_REGEX.find(lastText)?.groupValues?.get(1)?.trim() ?: "Unknown Artist"
            Timber.d("DreamGemini - Image generated, artist: $artistName")

            Result.success(DreamImage(imageBase64 = imageData.data, mimeType = imageData.mimeType, artistName = artistName))
        } catch (e: IOException) {
            Timber.e(e, "DreamGemini - Network error during image generation")
            Result.failure(e)
        } catch (e: HttpException) {
            Timber.e(e, "DreamGemini - HTTP error during image generation (code=${e.code()})")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "DreamGemini - Unexpected error during image generation")
            Result.failure(e)
        }
    }

    @Suppress("LongMethod")
    private fun parseInterpretation(json: String): DreamInterpretation {
        val parsed = gson.fromJson(json, GeminiDreamResponse::class.java)

        Timber.d(
            "DreamGemini - Parsed palette: sky=%d (0x%X), horizon=%d (0x%X), accent=%d (0x%X)",
            parsed.scene.palette.sky, parsed.scene.palette.sky,
            parsed.scene.palette.horizon, parsed.scene.palette.horizon,
            parsed.scene.palette.accent, parsed.scene.palette.accent,
        )
        Timber.d("DreamGemini - Parsed %d layers, %d particle types", parsed.scene.layers.size, parsed.scene.particles.size)
        parsed.scene.layers.forEachIndexed { i, layer ->
            Timber.d("DreamGemini - Layer %d: %d elements, depth=%.2f", i, layer.elements.size, layer.depth)
            layer.elements.forEach { e ->
                Timber.d(
                    "DreamGemini -   %s at (%.2f,%.2f) scale=%.2f color=%d (0x%X) alpha=%.2f",
                    e.shape, e.x, e.y, e.scale, e.color, e.color, e.alpha,
                )
            }
        }

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
        private const val IMAGE_GENERATION_MAX_RETRIES = 3
        private const val RETRY_LOG_PREVIEW_LENGTH = 80
        private val ARTIST_REGEX = Regex("""Artist:\s*(.+)""", RegexOption.IGNORE_CASE)
    }
}

// Alpha channel constants for color correction
private const val ALPHA_MASK = 0xFF000000L
private const val ALPHA_SHIFT = 24
private const val ALPHA_CHANNEL = 0xFFL

/**
 * Forces the alpha channel to 0xFF when it is 0x00. Gemini often returns RGB color values
 * (e.g. 16711680 = 0x00FF0000) without the alpha byte, making them fully transparent.
 */
private fun ensureAlpha(color: Long): Long {
    return if ((color shr ALPHA_SHIFT) and ALPHA_CHANNEL == 0L) {
        color or ALPHA_MASK
    } else {
        color
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
    fun toDomain() = se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette(
        sky = ensureAlpha(sky),
        horizon = ensureAlpha(horizon),
        accent = ensureAlpha(accent),
    )
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
        x = x, y = y, scale = scale, color = ensureAlpha(color), alpha = alpha,
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
        count = count, color = ensureAlpha(color), speed = speed, size = size,
    )
}
