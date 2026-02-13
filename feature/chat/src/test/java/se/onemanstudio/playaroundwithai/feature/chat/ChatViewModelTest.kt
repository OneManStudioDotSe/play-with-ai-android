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
import org.junit.Rule
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GenerateContentUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetFailedSyncCountUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetPromptHistoryUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetSuggestionsUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetSyncStateUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.SavePromptUseCase
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatError
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatUiState
import se.onemanstudio.playaroundwithai.feature.chat.util.MainCoroutineRule
import java.io.IOException
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainCoroutineRule(UnconfinedTestDispatcher())

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
        val viewModel = createViewModel(
            generateContentResult = Result.success("Mocked AI response")
        )
        val states = mutableListOf<ChatUiState>()

        // When
        viewModel.uiState
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.generateContent(prompt, null)
        advanceUntilIdle()

        // Then
        assertEquals(expected = ChatUiState.Initial, actual = states[0])
        assertEquals(expected = ChatUiState.Loading, actual = states[1])
        assert(states[2] is ChatUiState.Success)
        assertEquals(expected = "Mocked AI response", actual = (states[2] as ChatUiState.Success).outputText)
    }

    @Test
    fun `generateContent failure updates state to unknown error`() = runTest {
        // Given
        val prompt = "What is the meaning of life?"
        val exception = RuntimeException("Test error")
        val viewModel = createViewModel(
            generateContentResult = Result.failure(exception)
        )
        val states = mutableListOf<ChatUiState>()

        // When
        viewModel.uiState
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.generateContent(prompt, null)
        advanceUntilIdle()

        // Then
        assertEquals(expected = ChatUiState.Initial, actual = states[0])
        assertEquals(expected = ChatUiState.Loading, actual = states[1])
        assert(states[2] is ChatUiState.Error)
        assertEquals(expected = ChatError.Unknown(exception.message), actual = (states[2] as ChatUiState.Error).error)
    }

    @Test
    fun `generateContent network failure updates state to Network Error`() = runTest {
        // Given
        val prompt = "Prompt that will be used for testing network errors"
        val exception = IOException("Oh no, no internet!")
        val viewModel = createViewModel(
            generateContentResult = Result.failure(exception)
        )
        val states = mutableListOf<ChatUiState>()

        // When
        viewModel.uiState
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.generateContent(prompt, null)
        advanceUntilIdle()

        // Then
        assertEquals(expected = ChatUiState.Initial, actual = states[0])
        assertEquals(expected = ChatUiState.Loading, actual = states[1])
        assert(states[2] is ChatUiState.Error)
        assertEquals(expected = ChatError.NetworkMissing, actual = (states[2] as ChatUiState.Error).error)
    }

    @Test
    fun `init loads suggestions successfully`() = runTest {
        // Given
        val expectedSuggestions = listOf("Topic 1", "Topic 2", "Topic 3")
        val viewModel = createViewModel(
            suggestionsResult = Result.success(expectedSuggestions)
        )

        // When
        // (Init happens automatically on creation)
        advanceUntilIdle() // Ensure coroutine in init block finishes

        // Then
        assertEquals(expectedSuggestions, viewModel.suggestions.value)
    }

    @Test
    fun `init loads fallback suggestions on failure`() = runTest {
        // Given
        val failureResult = Result.failure<List<String>>(Exception("API Error"))
        // These hardcoded strings must match what you put in ChatViewModel's onFailure block
        val fallbackSuggestions = listOf("Tell me a joke", "Explain Quantum Physics", "Roast my code")

        val viewModel = createViewModel(
            suggestionsResult = failureResult
        )

        // When
        advanceUntilIdle()

        // Then
        assertEquals(fallbackSuggestions, viewModel.suggestions.value)
    }

    private fun createViewModel(
        generateContentResult: Result<String>? = null,
        suggestionsResult: Result<List<String>> = Result.success(emptyList()),
        promptHistoryResult: List<Prompt> = emptyList(),
        isSyncingResult: Boolean = false,
        failedSyncCountResult: Int = 0
    ): ChatViewModel {
        val geminiRepository = mockk<GeminiRepository> {
            generateContentResult?.let { coEvery { generateContent(any(), any(), any(), any()) } returns it }
            coEvery { generateConversationStarters() } returns suggestionsResult
        }

        val promptRepository = mockk<PromptRepository> {
            coEvery { savePrompt(any()) } returns Unit
            every { getPromptHistory() } returns flowOf(promptHistoryResult)
            every { isSyncing() } returns flowOf(isSyncingResult)
            every { getFailedSyncCount() } returns flowOf(failedSyncCountResult)
        }

        val application = mockk<Application>(relaxed = true)

        return ChatViewModel(
            GenerateContentUseCase(geminiRepository),
            GetSuggestionsUseCase(geminiRepository),
            GetPromptHistoryUseCase(promptRepository),
            GetSyncStateUseCase(promptRepository),
            GetFailedSyncCountUseCase(promptRepository),
            SavePromptUseCase(promptRepository),
            application
        )
    }
}
