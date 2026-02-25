package se.onemanstudio.playaroundwithai.data.dream.data.repository

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.dto.Candidate
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiResponse
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.network.dto.UsageMetadata
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DreamGeminiRepositoryImplTest {

    private lateinit var apiService: GeminiApiService
    private lateinit var tokenUsageTracker: TokenUsageTracker
    private lateinit var repository: DreamGeminiRepositoryImpl

    private val gson = Gson()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        apiService = mockk()
        tokenUsageTracker = mockk(relaxed = true)
        repository = DreamGeminiRepositoryImpl(apiService, gson, tokenUsageTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `interpretDream with valid JSON returns successful DreamInterpretation`() = runTest {
        coEvery { apiService.generateContent(any()) } returns geminiResponse(VALID_JSON)

        val result = repository.interpretDream("I was flying over a purple ocean")

        assertThat(result.isSuccess).isTrue()
        val interpretation = result.getOrThrow()
        assertThat(interpretation.textAnalysis).isEqualTo("Your dream symbolizes freedom.")
        assertThat(interpretation.mood).isEqualTo(DreamMood.JOYFUL)
        assertThat(interpretation.scene.palette.sky).isEqualTo(4278190335L)
        assertThat(interpretation.scene.palette.horizon).isEqualTo(4278255615L)
        assertThat(interpretation.scene.palette.accent).isEqualTo(4294901760L)
        assertThat(interpretation.scene.layers).isEmpty()
        assertThat(interpretation.scene.particles).hasSize(1)
        assertThat(interpretation.scene.particles[0].shape).isEqualTo(ParticleShape.DOT)
        assertThat(interpretation.scene.particles[0].count).isEqualTo(10)
        assertThat(interpretation.scene.particles[0].color).isEqualTo(4294967295L)
        assertThat(interpretation.scene.particles[0].speed).isEqualTo(1.0f)
        assertThat(interpretation.scene.particles[0].size).isEqualTo(4.0f)
    }

    @Test
    fun `interpretDream when API returns null text returns failure`() = runTest {
        coEvery { apiService.generateContent(any()) } returns GeminiResponse(
            candidates = listOf(Candidate(content = Content(parts = emptyList())))
        )

        val result = repository.interpretDream("A strange dream")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(Exception::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("No response text from Gemini")
    }

    @Test
    fun `interpretDream when API throws IOException returns failure`() = runTest {
        coEvery { apiService.generateContent(any()) } throws IOException("Network error")

        val result = repository.interpretDream("A dream about the sea")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Network error")
    }

    @Test
    fun `interpretDream when API throws HttpException returns failure`() = runTest {
        val httpException = HttpException(Response.error<Any>(429, "Rate limited".toResponseBody()))
        coEvery { apiService.generateContent(any()) } throws httpException

        val result = repository.interpretDream("A dream about the sky")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
    }

    @Test
    fun `interpretDream with malformed JSON returns failure with JsonSyntaxException`() = runTest {
        coEvery { apiService.generateContent(any()) } returns geminiResponse("{ not valid json !!!")

        val result = repository.interpretDream("A dream about chaos")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(JsonSyntaxException::class.java)
    }

    @Test
    fun `interpretDream strips json code fences from response`() = runTest {
        val wrappedJson = "```json\n$VALID_JSON\n```"
        coEvery { apiService.generateContent(any()) } returns geminiResponse(wrappedJson)

        val result = repository.interpretDream("I was swimming in clouds")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().textAnalysis).isEqualTo("Your dream symbolizes freedom.")
        assertThat(result.getOrThrow().mood).isEqualTo(DreamMood.JOYFUL)
    }

    @Test
    fun `interpretDream with unknown mood string defaults to MYSTERIOUS`() = runTest {
        val jsonWithUnknownMood = VALID_JSON.replace("\"JOYFUL\"", "\"WHIMSICAL\"")
        coEvery { apiService.generateContent(any()) } returns geminiResponse(jsonWithUnknownMood)

        val result = repository.interpretDream("A peculiar dream")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().mood).isEqualTo(DreamMood.MYSTERIOUS)
    }

    @Test
    fun `interpretDream records token usage`() = runTest {
        val usageMetadata = UsageMetadata(promptTokenCount = 100, candidatesTokenCount = 200, totalTokenCount = 300)
        coEvery { apiService.generateContent(any()) } returns geminiResponse(VALID_JSON, usageMetadata)

        repository.interpretDream("I was flying")

        coVerify(exactly = 1) { tokenUsageTracker.record("dream", usageMetadata) }
    }

    @Test
    fun `interpretDream sends prompt containing the dream description`() = runTest {
        val requestSlot = slot<GeminiRequest>()
        coEvery { apiService.generateContent(capture(requestSlot)) } returns geminiResponse(VALID_JSON)

        repository.interpretDream("I was lost in a forest")

        val sentText = requestSlot.captured.contents.first().parts.first().text!!
        assertThat(sentText).contains("I was lost in a forest")
    }

    private fun geminiResponse(text: String, usageMetadata: UsageMetadata? = null): GeminiResponse {
        return GeminiResponse(
            candidates = listOf(Candidate(content = Content(parts = listOf(Part(text = text))))),
            usageMetadata = usageMetadata,
        )
    }

    companion object {
        private val VALID_JSON = """
            {
              "interpretation": "Your dream symbolizes freedom.",
              "mood": "JOYFUL",
              "scene": {
                "palette": { "sky": 4278190335, "horizon": 4278255615, "accent": 4294901760 },
                "layers": [],
                "particles": [{ "shape": "DOT", "count": 10, "color": 4294967295, "speed": 1.0, "size": 4.0 }]
              }
            }
        """.trimIndent()
    }
}
