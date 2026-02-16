package se.onemanstudio.playaroundwithai.feature.chat

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.RetryPendingSyncsUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.SavePromptUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.UpdatePromptTextUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.usecase.ObserveAuthReadyUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.feature.chat.models.SnackbarEvent
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatError
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatUiState
import se.onemanstudio.playaroundwithai.feature.chat.util.FileUtils
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

        val viewModel = createViewModel(
            suggestionsResult = failureResult
        )

        // When
        advanceUntilIdle()

        // Then - application.getString() returns "" for relaxed mock, so we get 3 empty strings
        assertEquals(3, viewModel.suggestions.value.size)
    }

    @Test
    fun `isSyncing reflects sync state use case`() = runTest {
        // Given
        val viewModel = createViewModel(isSyncingResult = true)

        // When
        val states = mutableListOf<Boolean>()
        viewModel.isSyncing
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        advanceUntilIdle()

        // Then
        assertEquals(expected = true, actual = states.last())
    }

    @Test
    fun `isSyncing defaults to false when not syncing`() = runTest {
        // Given
        val viewModel = createViewModel(isSyncingResult = false)

        // When
        val states = mutableListOf<Boolean>()
        viewModel.isSyncing
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        advanceUntilIdle()

        // Then
        assertEquals(expected = false, actual = states.last())
    }

    @Test
    fun `generateContent saves question first then updates with response`() = runTest {
        // Given
        val prompt = "Test question"
        val response = "Test response"
        val promptRepository = mockk<PromptRepository> {
            coEvery { savePrompt(any()) } returns 1L
            coEvery { updatePromptText(any(), any()) } returns Unit
            every { getPromptHistory() } returns flowOf(emptyList())
            every { isSyncing() } returns flowOf(false)
            every { getFailedSyncCount() } returns flowOf(0)
        }
        val viewModel = createViewModel(
            generateContentResult = Result.success(response),
            promptRepository = promptRepository
        )

        // When
        viewModel.generateContent(prompt, null)
        advanceUntilIdle()

        // Then
        coVerify { promptRepository.savePrompt(match { it.text == prompt }) }
        coVerify { promptRepository.updatePromptText(1L, "Q: $prompt\nA: $response") }
    }

    @Test
    fun `generateContent emits LocalSaveFailed when save throws`() = runTest {
        // Given
        val prompt = "Test question"
        val promptRepository = mockk<PromptRepository> {
            coEvery { savePrompt(any()) } throws RuntimeException("DB error")
            every { getPromptHistory() } returns flowOf(emptyList())
            every { isSyncing() } returns flowOf(false)
            every { getFailedSyncCount() } returns flowOf(0)
        }
        val viewModel = createViewModel(
            generateContentResult = Result.success("response"),
            promptRepository = promptRepository
        )
        val events = mutableListOf<SnackbarEvent>()

        // When
        viewModel.snackbarEvent
            .onEach { events.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.generateContent(prompt, null)
        advanceUntilIdle()

        // Then
        assert(events.any { it is SnackbarEvent.LocalSaveFailed })
    }

    @Test
    fun `generateContent emits LocalUpdateFailed when update throws`() = runTest {
        // Given
        val prompt = "Test question"
        val promptRepository = mockk<PromptRepository> {
            coEvery { savePrompt(any()) } returns 1L
            coEvery { updatePromptText(any(), any()) } throws RuntimeException("DB error")
            every { getPromptHistory() } returns flowOf(emptyList())
            every { isSyncing() } returns flowOf(false)
            every { getFailedSyncCount() } returns flowOf(0)
        }
        val viewModel = createViewModel(
            generateContentResult = Result.success("response"),
            promptRepository = promptRepository
        )
        val events = mutableListOf<SnackbarEvent>()

        // When
        viewModel.snackbarEvent
            .onEach { events.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.generateContent(prompt, null)
        advanceUntilIdle()

        // Then
        assert(events.any { it is SnackbarEvent.LocalUpdateFailed })
    }

    @Test
    fun `retryFailedSyncs calls use case`() = runTest {
        // Given
        val promptRepository = mockk<PromptRepository> {
            coEvery { savePrompt(any()) } returns 1L
            coEvery { retryPendingSyncs() } returns Unit
            every { getPromptHistory() } returns flowOf(emptyList())
            every { isSyncing() } returns flowOf(false)
            every { getFailedSyncCount() } returns flowOf(0)
        }
        val viewModel = createViewModel(promptRepository = promptRepository)

        // When
        viewModel.retryFailedSyncs()
        advanceUntilIdle()

        // Then
        coVerify { promptRepository.retryPendingSyncs() }
    }

    @Test
    fun `missing Gemini API key sets ApiKeyMissing error on init`() = runTest {
        // Given
        val viewModel = createViewModel(
            apiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = false, isMapsKeyAvailable = true)
        )
        val states = mutableListOf<ChatUiState>()

        // When
        viewModel.uiState
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        advanceUntilIdle()

        // Then
        assertEquals(ChatUiState.Error(ChatError.ApiKeyMissing), states.last())
    }

    @Test
    fun `generateContent returns ApiKeyMissing when Gemini key is missing`() = runTest {
        // Given
        val viewModel = createViewModel(
            generateContentResult = Result.success("response"),
            apiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = false, isMapsKeyAvailable = true)
        )
        val states = mutableListOf<ChatUiState>()

        // When
        viewModel.uiState
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.generateContent("test", null)
        advanceUntilIdle()

        // Then - should stay as ApiKeyMissing, never go to Loading
        assert(states.none { it is ChatUiState.Loading })
        assertEquals(ChatUiState.Error(ChatError.ApiKeyMissing), states.last())
    }

    private fun createViewModel(
        generateContentResult: Result<String>? = null,
        suggestionsResult: Result<List<String>> = Result.success(emptyList()),
        promptHistoryResult: List<Prompt> = emptyList(),
        isSyncingResult: Boolean = false,
        failedSyncCountResult: Int = 0,
        promptRepository: PromptRepository? = null,
        apiKeyAvailability: ApiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = true, isMapsKeyAvailable = true)
    ): ChatViewModel {
        val geminiRepository = mockk<GeminiRepository> {
            generateContentResult?.let { coEvery { generateContent(any(), any(), any(), any(), any()) } returns it }
            coEvery { generateConversationStarters(any()) } returns suggestionsResult
        }

        val effectivePromptRepository = promptRepository ?: mockk<PromptRepository> {
            coEvery { savePrompt(any()) } returns 1L
            coEvery { updatePromptText(any(), any()) } returns Unit
            coEvery { retryPendingSyncs() } returns Unit
            every { getPromptHistory() } returns flowOf(promptHistoryResult)
            every { isSyncing() } returns flowOf(isSyncingResult)
            every { getFailedSyncCount() } returns flowOf(failedSyncCountResult)
        }

        val application = mockk<Application>(relaxed = true)
        val fileUtils = mockk<FileUtils>(relaxed = true)
        val authReadyFlow: StateFlow<Boolean> = MutableStateFlow(true)
        val observeAuthReadyUseCase = mockk<ObserveAuthReadyUseCase>()
        every { observeAuthReadyUseCase.invoke() } returns authReadyFlow

        return ChatViewModel(
            GenerateContentUseCase(geminiRepository),
            GetSuggestionsUseCase(geminiRepository),
            GetPromptHistoryUseCase(effectivePromptRepository),
            GetSyncStateUseCase(effectivePromptRepository),
            GetFailedSyncCountUseCase(effectivePromptRepository),
            SavePromptUseCase(effectivePromptRepository),
            UpdatePromptTextUseCase(effectivePromptRepository),
            RetryPendingSyncsUseCase(effectivePromptRepository),
            observeAuthReadyUseCase,
            apiKeyAvailability,
            fileUtils,
            application
        )
    }
}
