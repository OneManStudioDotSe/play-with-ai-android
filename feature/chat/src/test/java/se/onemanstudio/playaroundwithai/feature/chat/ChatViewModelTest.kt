package se.onemanstudio.playaroundwithai.feature.chat

import android.app.Application
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.data.domain.model.Prompt
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.GeminiRepository
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.model.GeminiResponse
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    @Test
    fun `Initial state is Idle`() = runTest {
        // Given
        val viewModel = createViewModel()
        val states = mutableListOf<ChatUiState>()

        // When
        viewModel.uiState
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        advanceUntilIdle()

        // Then
        assertEquals(1, states.size)
        assertEquals(ChatUiState.Initial, states[0])
    }

    @Test
    fun `generateContent success updates state to Success`() = runTest {
        // Given
        val prompt = "What is the meaning of life?"
        val fakeResponse = GeminiResponse(candidates = emptyList())
        val viewModel = createViewModel(
            geminiResult = Result.success(fakeResponse)
        )
        val states = mutableListOf<ChatUiState>()

        // When
        viewModel.uiState
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.generateContent(prompt, null)
        advanceUntilIdle()

        // Then
        assertEquals(ChatUiState.Initial, states[0])
        assertEquals(ChatUiState.Loading, states[1])
        assert(states[2] is ChatUiState.Success)
        assertEquals("No response text found.", (states[2] as ChatUiState.Success).outputText)
    }

    @Test
    fun `generateContent failure updates state to Error`() = runTest {
        // Given
        val prompt = "What is the meaning of life?"
        val exception = RuntimeException("Test error")
        val viewModel = createViewModel(
            geminiResult = Result.failure(exception)
        )
        val states = mutableListOf<ChatUiState>()

        // When
        viewModel.uiState
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.generateContent(prompt, null)
        advanceUntilIdle()

        // Then
        assertEquals(ChatUiState.Initial, states[0])
        assertEquals(ChatUiState.Loading, states[1])
        assert(states[2] is ChatUiState.Error)
        assertEquals("Test error", (states[2] as ChatUiState.Error).errorMessage)
    }

    private fun createViewModel(
        geminiResult: Result<GeminiResponse>? = null,
        promptHistoryResult: List<Prompt> = emptyList(),
    ): ChatViewModel {
        val repository = mockk<GeminiRepository> {
            geminiResult?.let { coEvery { generateContent(any(), any(), any(), any()) } returns it }
            coEvery { savePrompt(any()) } returns Unit
            every { getPromptHistory() } returns flowOf(promptHistoryResult)
        }

        val application = mockk<Application>(relaxed = true)

        return ChatViewModel(repository, application)
    }
}