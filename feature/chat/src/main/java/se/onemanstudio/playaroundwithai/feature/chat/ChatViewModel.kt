package se.onemanstudio.playaroundwithai.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.auth.usecase.ObserveAuthReadyUseCase
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.feature.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.feature.chat.domain.model.SyncStatus
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.AskAiUseCase
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.GetFailedSyncCountUseCase
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.GetPromptHistoryUseCase
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.GetSuggestionsUseCase
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.GetSyncStateUseCase
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.RetryPendingSyncsUseCase
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.SavePromptUseCase
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.UpdatePromptTextUseCase
import se.onemanstudio.playaroundwithai.feature.chat.models.Attachment
import se.onemanstudio.playaroundwithai.feature.chat.models.SnackbarEvent
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatError
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatScreenState
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatUiState
import se.onemanstudio.playaroundwithai.feature.chat.util.FileUtils
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.time.Instant
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val askAiUseCase: AskAiUseCase,
    private val getSuggestionsUseCase: GetSuggestionsUseCase,
    getPromptHistoryUseCase: GetPromptHistoryUseCase,
    getSyncStateUseCase: GetSyncStateUseCase,
    private val getFailedSyncCountUseCase: GetFailedSyncCountUseCase,
    private val savePromptUseCase: SavePromptUseCase,
    private val updatePromptTextUseCase: UpdatePromptTextUseCase,
    private val retryPendingSyncsUseCase: RetryPendingSyncsUseCase,
    private val observeAuthReadyUseCase: ObserveAuthReadyUseCase,
    private val apiKeyAvailability: ApiKeyAvailability,
    private val fileUtils: FileUtils,
) : ViewModel() {

    private val _screenState = MutableStateFlow(ChatScreenState())
    val screenState = _screenState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val snackbarEvent: SharedFlow<SnackbarEvent> = _snackbarEvent.asSharedFlow()

    init {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _screenState.update { it.copy(chatState = ChatUiState.Error(ChatError.ApiKeyMissing)) }
        } else {
            loadSuggestionsAfterAuth()
            observeSyncFailures()
        }

        observePromptHistory(getPromptHistoryUseCase)
        observeSyncState(getSyncStateUseCase)
    }

    private fun observePromptHistory(getPromptHistoryUseCase: GetPromptHistoryUseCase) {
        viewModelScope.launch {
            getPromptHistoryUseCase().collect { history ->
                _screenState.update { it.copy(promptHistory = history) }
            }
        }
    }

    private fun observeSyncState(getSyncStateUseCase: GetSyncStateUseCase) {
        viewModelScope.launch {
            getSyncStateUseCase().collect { syncing ->
                _screenState.update { it.copy(isSyncing = syncing) }
            }
        }
    }

    private fun loadSuggestionsAfterAuth() {
        viewModelScope.launch {
            observeAuthReadyUseCase().first { it }
            loadSuggestions()
        }
    }

    private fun observeSyncFailures() {
        viewModelScope.launch {
            getFailedSyncCountUseCase()
                .filter { it > 0 }
                .collect { count ->
                    _snackbarEvent.tryEmit(SnackbarEvent.SyncFailed(count))
                }
        }
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            _screenState.update { it.copy(isSuggestionsLoading = true) }
            getSuggestionsUseCase()
                .onSuccess { topics ->
                    _screenState.update { it.copy(suggestions = topics) }
                }
                .onFailure {
                    _screenState.update { it.copy(useFallbackSuggestions = true) }
                }

            _screenState.update { it.copy(isSuggestionsLoading = false) }
        }
    }

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    fun sendPrompt(prompt: String, attachment: Attachment?) {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _screenState.update { it.copy(chatState = ChatUiState.Error(ChatError.ApiKeyMissing)) }
            return
        }

        _screenState.update { it.copy(chatState = ChatUiState.Loading) }

        viewModelScope.launch {
            val imageBytes = (attachment as? Attachment.Image)?.uri?.let {
                fileUtils.uriToByteArray(it)
            }
            val analysisType = (attachment as? Attachment.Image)?.analysisType

            val fileResult = (attachment as? Attachment.Document)?.uri?.let {
                fileUtils.extractFileContent(it)
            }

            if (fileResult != null && fileResult.isFailure) {
                val error = when (fileResult.exceptionOrNull()) {
                    is FileNotFoundException -> ChatError.FileNotFound
                    is SecurityException -> ChatError.Permission
                    else -> ChatError.FileRead
                }

                _screenState.update { it.copy(chatState = ChatUiState.Error(error)) }

                return@launch
            }

            val fileText = fileResult?.getOrNull()

            val savedId = try {
                savePromptUseCase(
                    Prompt(
                        text = prompt,
                        timestamp = Instant.now(),
                        syncStatus = SyncStatus.Pending,
                        imageAttachment = imageBytes,
                        documentAttachment = fileText
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "ChatVM - Failed to save prompt to local DB")
                _snackbarEvent.tryEmit(SnackbarEvent.LocalSaveFailed)
                null
            }

            askAiUseCase(
                prompt = prompt,
                imageBytes = imageBytes,
                fileText = fileText,
                analysisType = analysisType,
            ).onSuccess { responseText ->
                _screenState.update { it.copy(chatState = ChatUiState.Success(responseText)) }

                if (savedId != null) {
                    try {
                        updatePromptTextUseCase(savedId, "Q: $prompt\nA: $responseText")
                    } catch (e: Exception) {
                        Timber.e(e, "ChatVM - Failed to update prompt text in local DB")
                        _snackbarEvent.tryEmit(SnackbarEvent.LocalUpdateFailed)
                    }
                }
            }.onFailure { exception ->
                val error = when (exception) {
                    is IOException -> ChatError.NetworkMissing
                    is SecurityException -> ChatError.Permission
                    else -> ChatError.Unknown(exception.localizedMessage)
                }

                _screenState.update { it.copy(chatState = ChatUiState.Error(error)) }
            }
        }
    }

    fun retryFailedSyncs() {
        viewModelScope.launch {
            retryPendingSyncsUseCase()
        }
    }

    fun restoreAnswer(answer: String) {
        _screenState.update { it.copy(chatState = ChatUiState.Success(answer)) }
    }

    fun clearResponse() {
        _screenState.update { it.copy(chatState = ChatUiState.Initial) }
    }
}
