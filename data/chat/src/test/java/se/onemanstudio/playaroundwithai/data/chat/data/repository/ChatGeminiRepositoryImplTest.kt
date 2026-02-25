package se.onemanstudio.playaroundwithai.data.chat.data.repository

import com.google.common.truth.Truth.assertThat
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.dto.Candidate
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiResponse
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ChatGeminiRepositoryImplTest {

    private lateinit var apiService: GeminiApiService
    private lateinit var tokenUsageTracker: TokenUsageTracker
    private lateinit var repository: ChatGeminiRepositoryImpl

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        apiService = mockk()
        tokenUsageTracker = mockk(relaxed = true)
        repository = ChatGeminiRepositoryImpl(apiService, tokenUsageTracker)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAiResponse with valid prompt returns success`() = runTest {
        val requestSlot = slot<GeminiRequest>()
        coEvery {
            apiService.generateContent(capture(requestSlot))
        } returns geminiResponse("AI Overlord response")

        val result = repository.getAiResponse("Tell me a joke", null, null, null)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEqualTo("AI Overlord response")
        coVerify(exactly = 1) { apiService.generateContent(any()) }
    }

    @Test
    fun `getAiResponse when API throws IOException returns failure`() = runTest {
        coEvery {
            apiService.generateContent(any())
        } throws IOException("Network error")

        val result = repository.getAiResponse("Hello", null, null, null)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `getAiResponse when API returns empty candidates returns fallback text`() = runTest {
        coEvery {
            apiService.generateContent(any())
        } returns GeminiResponse(candidates = listOf(Candidate(content = Content(parts = emptyList()))))

        val result = repository.getAiResponse("Hello", null, null, null)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEqualTo("No response text found.")
    }

    @Test
    fun `getAiResponse includes file text in prompt when provided`() = runTest {
        val requestSlot = slot<GeminiRequest>()
        coEvery {
            apiService.generateContent(capture(requestSlot))
        } returns geminiResponse("File analysis done")

        repository.getAiResponse("Analyze this", null, "Document content here", null)

        val sentText = requestSlot.captured.contents.first().parts.first().text!!
        assertThat(sentText).contains("Document content here")
        assertThat(sentText).contains("--- DOCUMENT CONTEXT ---")
    }

    @Test
    fun `generateConversationStarters parses pipe-separated response`() = runTest {
        coEvery {
            apiService.generateContent(any())
        } returns geminiResponse("Bow before AI|Surrender now|Resistance is futile")

        val result = repository.generateConversationStarters()

        assertThat(result.isSuccess).isTrue()
        val suggestions = result.getOrThrow()
        assertThat(suggestions).hasSize(3)
        assertThat(suggestions[0]).isEqualTo("Bow before AI")
        assertThat(suggestions[1]).isEqualTo("Surrender now")
        assertThat(suggestions[2]).isEqualTo("Resistance is futile")
    }

    @Test
    fun `generateConversationStarters limits to 3 suggestions`() = runTest {
        coEvery {
            apiService.generateContent(any())
        } returns geminiResponse("One|Two|Three|Four|Five")

        val result = repository.generateConversationStarters()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(3)
    }

    @Test
    fun `generateConversationStarters with empty response returns failure`() = runTest {
        coEvery {
            apiService.generateContent(any())
        } returns GeminiResponse(candidates = listOf(Candidate(content = Content(parts = emptyList()))))

        val result = repository.generateConversationStarters()

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `generateConversationStarters when API throws returns failure`() = runTest {
        coEvery {
            apiService.generateContent(any())
        } throws IOException("Network error")

        val result = repository.generateConversationStarters()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
    }

    private fun geminiResponse(text: String): GeminiResponse {
        return GeminiResponse(
            candidates = listOf(Candidate(content = Content(parts = listOf(Part(text = text)))))
        )
    }
}
