package se.onemanstudio.playaroundwithai.core.data.feature.chat.repository

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Candidate
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Content
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.GeminiResponse
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto.Part
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.GeminiModel
import java.io.IOException

class GeminiRepositoryImplUnitTest {

    private lateinit var apiService: GeminiApiService
    private lateinit var gson: Gson
    private lateinit var repository: GeminiRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        gson = Gson()
        repository = GeminiRepositoryImpl(apiService, gson)
    }

    // region getAiResponse

    @Test
    fun `getAiResponsewith valid prompt returns success`() = runTest {
        // GIVEN: API returns a valid response
        val response = createGeminiResponse("AI response text")
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.getAiResponse("Hello", null, null, null, GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("AI response text")
    }

    @Test
    fun `getAiResponsewhen API throws IOException returns failure`() = runTest {
        // GIVEN: API throws a network error
        coEvery { apiService.generateContent(any(), any()) } throws IOException("No network")

        // WHEN
        val result = repository.getAiResponse("Hello", null, null, null, GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `getAiResponsewhen API returns empty candidates returns fallback text`() = runTest {
        // GIVEN: API returns response with no candidates
        val response = GeminiResponse(candidates = emptyList())
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.getAiResponse("Hello", null, null, null, GeminiModel.FLASH_PREVIEW)

        // THEN: extractText() returns null, so fallback text is used
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("No response text found.")
    }

    // endregion

    // region generateConversationStarters

    @Test
    fun `generateConversationStarters parses pipe-separated response`() = runTest {
        // GIVEN: API returns suggestions in pipe-separated format
        val response = createGeminiResponse("Topic A|Topic B|Topic C")
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.generateConversationStarters(GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).containsExactly("Topic A", "Topic B", "Topic C")
    }

    @Test
    fun `generateConversationStarters limits to 3 suggestions`() = runTest {
        // GIVEN: API returns more than 3 suggestions
        val response = createGeminiResponse("A|B|C|D|E")
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.generateConversationStarters(GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).hasSize(3)
    }

    @Test
    fun `generateConversationStarters with empty response returns failure`() = runTest {
        // GIVEN: API returns empty text
        val response = createGeminiResponse("")
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.generateConversationStarters(GeminiModel.FLASH_PREVIEW)

        // THEN: No suggestions could be parsed
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `generateConversationStarters when API throws returns failure`() = runTest {
        // GIVEN: API throws
        coEvery { apiService.generateContent(any(), any()) } throws RuntimeException("Server error")

        // WHEN
        val result = repository.generateConversationStarters(GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isFailure).isTrue()
    }

    // endregion

    // region getSuggestedPlaces

    @Test
    fun `getSuggestedPlaces parses valid JSON response`() = runTest {
        // GIVEN: API returns valid JSON with places
        val jsonResponse = """
            {"places": [
                {"name": "Gamla Stan", "latitude": 59.32, "longitude": 18.07, "description": "Old Town", "category": "Historic"}
            ]}
        """.trimIndent()
        val response = createGeminiResponse(jsonResponse)
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.getSuggestedPlaces(59.3, 18.0, GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isSuccess).isTrue()
        val places = result.getOrNull()!!
        assertThat(places).hasSize(1)
        assertThat(places[0].name).isEqualTo("Gamla Stan")
    }

    @Test
    fun `getSuggestedPlaces with code-fenced JSON extracts correctly`() = runTest {
        // GIVEN: API wraps JSON in code fences
        val jsonResponse =
            "```json\n" +
            "{\"places\": [{\"name\": \"Park\", \"latitude\": 59.0, " +
            "\"longitude\": 18.0, \"description\": \"A park\", \"category\": \"Nature\"}]}\n```"
        val response = createGeminiResponse(jsonResponse)
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.getSuggestedPlaces(59.0, 18.0, GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()!!).hasSize(1)
    }

    @Test
    fun `getSuggestedPlaces with empty response returns failure`() = runTest {
        // GIVEN: API returns blank text
        val response = createGeminiResponse("   ")
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.getSuggestedPlaces(59.0, 18.0, GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `getSuggestedPlaces with malformed JSON returns failure`() = runTest {
        // GIVEN: API returns invalid JSON
        val response = createGeminiResponse("not valid json {{{")
        coEvery { apiService.generateContent(any(), any()) } returns response

        // WHEN
        val result = repository.getSuggestedPlaces(59.0, 18.0, GeminiModel.FLASH_PREVIEW)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(JsonSyntaxException::class.java)
    }

    // endregion

    private fun createGeminiResponse(text: String): GeminiResponse {
        return GeminiResponse(
            candidates = listOf(
                Candidate(content = Content(parts = listOf(Part(text = text))))
            )
        )
    }
}
