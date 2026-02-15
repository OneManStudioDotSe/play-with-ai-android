package se.onemanstudio.playaroundwithai.feature.chat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.usecase.ObserveAuthReadyUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GenerateContentUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetFailedSyncCountUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetPromptHistoryUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetSuggestionsUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetSyncStateUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.RetryPendingSyncsUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.SavePromptUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.UpdatePromptTextUseCase
import se.onemanstudio.playaroundwithai.feature.chat.models.Attachment
import se.onemanstudio.playaroundwithai.feature.chat.models.SnackbarEvent
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatError
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatUiState
import se.onemanstudio.playaroundwithai.feature.chat.util.FileUtils
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.time.Instant
import javax.inject.Inject

private const val SUBSCRIBE_TIMEOUT = 5000L

@Suppress("CanBeParameter", "LongParameterList", "TooManyFunctions")
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generateContentUseCase: GenerateContentUseCase,
    private val getSuggestionsUseCase: GetSuggestionsUseCase,
    private val getPromptHistoryUseCase: GetPromptHistoryUseCase,
    private val getSyncStateUseCase: GetSyncStateUseCase,
    private val getFailedSyncCountUseCase: GetFailedSyncCountUseCase,
    private val savePromptUseCase: SavePromptUseCase,
    private val updatePromptTextUseCase: UpdatePromptTextUseCase,
    private val retryPendingSyncsUseCase: RetryPendingSyncsUseCase,
    private val observeAuthReadyUseCase: ObserveAuthReadyUseCase,
    private val fileUtils: FileUtils,
    private val application: Application
) : ViewModel() {
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _isSuggestionsLoading = MutableStateFlow(false)
    val isSuggestionsLoading = _isSuggestionsLoading.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<SnackbarEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val snackbarEvent: SharedFlow<SnackbarEvent> = _snackbarEvent.asSharedFlow()

    val promptHistory: StateFlow<List<Prompt>> = getPromptHistoryUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT),
            initialValue = emptyList()
        )

    val isSyncing: StateFlow<Boolean> = getSyncStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIBE_TIMEOUT),
            initialValue = false
        )

    init {
        loadSuggestionsAfterAuth()
        observeSyncFailures()
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
                .distinctUntilChanged()
                .filter { it > 0 }
                .collect { count ->
                    _snackbarEvent.tryEmit(SnackbarEvent.SyncFailed(count))
                }
        }
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            _isSuggestionsLoading.value = true
            getSuggestionsUseCase()
                .onSuccess { topics ->
                    _suggestions.update { topics }
                }
                .onFailure {
                    // Fallback to static ones if API fails
                    _suggestions.update {
                        listOf(
                            application.getString(R.string.fallback_suggestion_joke),
                            application.getString(R.string.fallback_suggestion_physics),
                            application.getString(R.string.fallback_suggestion_roast)
                        )
                    }
                }

            _isSuggestionsLoading.value = false
        }
    }

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    fun generateContent(prompt: String, attachment: Attachment?) {
        _uiState.update { ChatUiState.Loading }

        viewModelScope.launch {
            // see if we have something attached (decode/compress off main thread)
            val imageBytes = (attachment as? Attachment.Image)?.uri?.let {
                withContext(Dispatchers.Default) { fileUtils.uriToByteArray(it) }
            }
            val analysisType = (attachment as? Attachment.Image)?.analysisType

            // read attached stuff
            val fileResult = (attachment as? Attachment.Document)?.uri?.let {
                withContext(Dispatchers.IO) { fileUtils.extractFileContent(it) }
            }

            // fail directly if something is off
            if (fileResult != null && fileResult.isFailure) {
                val error = when (fileResult.exceptionOrNull()) {
                    is FileNotFoundException -> ChatError.FileNotFound
                    is SecurityException -> ChatError.Permission
                    else -> ChatError.FileRead
                }

                _uiState.update { ChatUiState.Error(error) }

                return@launch
            }

            val fileText = fileResult?.getOrNull()

            // Save question to local DB immediately (before calling the API)
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
                _snackbarEvent.tryEmit(
                    SnackbarEvent.LocalSaveFailed(application.getString(R.string.local_save_failed))
                )
                null
            }

            generateContentUseCase(
                prompt = prompt,
                imageBytes = imageBytes,
                fileText = fileText,
                analysisType = analysisType,
            ).onSuccess { responseText ->
                _uiState.update { ChatUiState.Success(responseText) }

                // Update the saved entry with the full Q&A text
                if (savedId != null) {
                    try {
                        updatePromptTextUseCase(savedId, "Q: $prompt\nA: $responseText")
                    } catch (e: Exception) {
                        Timber.e(e, "ChatVM - Failed to update prompt text in local DB")
                        _snackbarEvent.tryEmit(
                            SnackbarEvent.LocalUpdateFailed(application.getString(R.string.local_update_failed))
                        )
                    }
                }
            }.onFailure { exception ->
                val error = when (exception) {
                    is IOException -> ChatError.NetworkMissing
                    is SecurityException -> ChatError.Permission
                    else -> ChatError.Unknown(exception.localizedMessage)
                }

                _uiState.update { ChatUiState.Error(error) }
            }
        }
    }

    fun retryFailedSyncs() {
        viewModelScope.launch {
            retryPendingSyncsUseCase()
        }
    }

    fun restoreAnswer(answer: String) {
        _uiState.update { ChatUiState.Success(answer) }
    }

    fun clearResponse() {
        _uiState.update { ChatUiState.Initial }
    }
}
