package se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.GeminiModel
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository

class GenerateContentUseCaseTest {

    private lateinit var geminiRepository: GeminiRepository
    private lateinit var useCase: GenerateContentUseCase

    @Before
    fun setUp() {
        geminiRepository = mockk()
        useCase = GenerateContentUseCase(geminiRepository)
    }

    @Test
    fun `invoke with valid prompt delegates to repository and returns success`() = runTest {
        // GIVEN: Repository returns a successful response
        val prompt = "Tell me a joke"
        val expectedResponse = "Why did the chicken cross the road?"
        coEvery {
            geminiRepository.generateContent(prompt, null, null, null, GeminiModel.FLASH_PREVIEW)
        } returns Result.success(expectedResponse)

        // WHEN
        val result = useCase(prompt)

        // THEN
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEqualTo(expectedResponse)
        coVerify(exactly = 1) { geminiRepository.generateContent(prompt, null, null, null, GeminiModel.FLASH_PREVIEW) }
    }

    @Test
    fun `invoke with blank prompt and no attachments returns failure`() = runTest {
        // GIVEN: A blank prompt with no image or file text

        // WHEN
        val result = useCase(prompt = "   ")

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Prompt and attachments cannot all be empty")
        coVerify(exactly = 0) { geminiRepository.generateContent(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke with blank prompt but valid image succeeds`() = runTest {
        // GIVEN: A blank prompt but with image bytes attached
        val imageBytes = byteArrayOf(1, 2, 3)
        coEvery {
            geminiRepository.generateContent("", imageBytes, null, null, GeminiModel.FLASH_PREVIEW)
        } returns Result.success("Image analysis result")

        // WHEN
        val result = useCase(prompt = "", imageBytes = imageBytes)

        // THEN
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEqualTo("Image analysis result")
        coVerify(exactly = 1) { geminiRepository.generateContent("", imageBytes, null, null, GeminiModel.FLASH_PREVIEW) }
    }

    @Test
    fun `invoke with blank prompt but valid file text succeeds`() = runTest {
        // GIVEN: A blank prompt but with file text attached
        val fileText = "Some document content"
        coEvery {
            geminiRepository.generateContent("", null, fileText, null, GeminiModel.FLASH_PREVIEW)
        } returns Result.success("File analysis result")

        // WHEN
        val result = useCase(prompt = "", fileText = fileText)

        // THEN
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEqualTo("File analysis result")
        coVerify(exactly = 1) { geminiRepository.generateContent("", null, fileText, null, GeminiModel.FLASH_PREVIEW) }
    }

    @Test
    fun `invoke with prompt exceeding max length returns failure`() = runTest {
        // GIVEN: A prompt that exceeds MAX_PROMPT_LENGTH
        val longPrompt = "a".repeat(MAX_PROMPT_LENGTH + 1)

        // WHEN
        val result = useCase(prompt = longPrompt)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("Prompt exceeds maximum length of ${MAX_PROMPT_LENGTH} characters")
        coVerify(exactly = 0) { geminiRepository.generateContent(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke with file text exceeding max length returns failure`() = runTest {
        // GIVEN: File text that exceeds MAX_FILE_TEXT_LENGTH
        val longFileText = "b".repeat(MAX_FILE_TEXT_LENGTH + 1)

        // WHEN
        val result = useCase(prompt = "Analyze this file", fileText = longFileText)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("File content exceeds maximum length of ${MAX_FILE_TEXT_LENGTH} characters")
        coVerify(exactly = 0) { geminiRepository.generateContent(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `invoke when repository fails propagates failure`() = runTest {
        // GIVEN: Repository returns a failure
        val prompt = "Tell me a joke"
        val expectedException = RuntimeException("API error")
        coEvery {
            geminiRepository.generateContent(prompt, null, null, null, GeminiModel.FLASH_PREVIEW)
        } returns Result.failure(expectedException)

        // WHEN
        val result = useCase(prompt)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(expectedException)
    }
}
