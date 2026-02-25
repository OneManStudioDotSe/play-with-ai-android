package se.onemanstudio.playaroundwithai.data.explore.data.repository

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiResponse
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.network.dto.UsageMetadata
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreSuggestionsRepositoryImplTest {

    private lateinit var apiService: GeminiApiService
    private lateinit var tokenUsageTracker: TokenUsageTracker
    private lateinit var repository: ExploreSuggestionsRepositoryImpl

    private val gson = Gson()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        apiService = mockk()
        tokenUsageTracker = mockk(relaxed = true)
        repository = ExploreSuggestionsRepositoryImpl(apiService, gson, tokenUsageTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getSuggestedPlaces with valid response returns list of SuggestedPlace`() = runTest {
        coEvery { apiService.generateContent(any()) } returns geminiResponse(VALID_JSON)

        val result = repository.getSuggestedPlaces(TEST_LAT, TEST_LNG)

        assertThat(result.isSuccess).isTrue()
        val places = result.getOrThrow()
        assertThat(places).hasSize(1)
        assertThat(places[0].name).isEqualTo("Royal Palace")
        assertThat(places[0].lat).isEqualTo(59.3268)
        assertThat(places[0].lng).isEqualTo(18.0717)
        assertThat(places[0].description).isEqualTo("Historic palace")
        assertThat(places[0].category).isEqualTo("Museum")
    }

    @Test
    fun `getSuggestedPlaces when response text is blank returns failure with IOException`() = runTest {
        coEvery { apiService.generateContent(any()) } returns geminiResponse("")

        val result = repository.getSuggestedPlaces(TEST_LAT, TEST_LNG)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
        assertThat(result.exceptionOrNull()!!.message).contains("No JSON response from Gemini")
    }

    @Test
    fun `getSuggestedPlaces when response text is null returns failure with IOException`() = runTest {
        coEvery { apiService.generateContent(any()) } returns GeminiResponse(
            candidates = listOf(Candidate(content = Content(parts = emptyList())))
        )

        val result = repository.getSuggestedPlaces(TEST_LAT, TEST_LNG)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `getSuggestedPlaces with code-fenced JSON extracts and parses correctly`() = runTest {
        val codeFencedJson = "```json\n$VALID_JSON\n```"
        coEvery { apiService.generateContent(any()) } returns geminiResponse(codeFencedJson)

        val result = repository.getSuggestedPlaces(TEST_LAT, TEST_LNG)

        assertThat(result.isSuccess).isTrue()
        val places = result.getOrThrow()
        assertThat(places).hasSize(1)
        assertThat(places[0].name).isEqualTo("Royal Palace")
    }

    @Test
    fun `getSuggestedPlaces with malformed JSON returns failure with JsonSyntaxException`() = runTest {
        coEvery { apiService.generateContent(any()) } returns geminiResponse("{ invalid json }")

        val result = repository.getSuggestedPlaces(TEST_LAT, TEST_LNG)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(JsonSyntaxException::class.java)
    }

    @Test
    fun `getSuggestedPlaces when API throws IOException returns failure`() = runTest {
        coEvery { apiService.generateContent(any()) } throws IOException("Network error")

        val result = repository.getSuggestedPlaces(TEST_LAT, TEST_LNG)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
        assertThat(result.exceptionOrNull()!!.message).isEqualTo("Network error")
    }

    @Test
    fun `getSuggestedPlaces when API throws HttpException returns failure`() = runTest {
        val httpException = HttpException(Response.error<Any>(429, "Too Many Requests".toResponseBody()))
        coEvery { apiService.generateContent(any()) } throws httpException

        val result = repository.getSuggestedPlaces(TEST_LAT, TEST_LNG)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
    }

    @Test
    fun `getSuggestedPlaces records token usage on successful API call`() = runTest {
        val usageMetadata = UsageMetadata(promptTokenCount = 100, candidatesTokenCount = 200, totalTokenCount = 300)
        coEvery { apiService.generateContent(any()) } returns geminiResponse(VALID_JSON, usageMetadata)

        repository.getSuggestedPlaces(TEST_LAT, TEST_LNG)

        coVerify(exactly = 1) { tokenUsageTracker.record("explore", usageMetadata) }
    }

    private fun geminiResponse(text: String, usageMetadata: UsageMetadata? = null): GeminiResponse {
        return GeminiResponse(
            candidates = listOf(Candidate(content = Content(parts = listOf(Part(text = text))))),
            usageMetadata = usageMetadata,
        )
    }

    companion object {
        private const val TEST_LAT = 59.3293
        private const val TEST_LNG = 18.0686

        private val VALID_JSON = """
            {
              "places": [
                {
                  "name": "Royal Palace",
                  "latitude": 59.3268,
                  "longitude": 18.0717,
                  "description": "Historic palace",
                  "category": "Museum"
                }
              ]
            }
        """.trimIndent()
    }
}
