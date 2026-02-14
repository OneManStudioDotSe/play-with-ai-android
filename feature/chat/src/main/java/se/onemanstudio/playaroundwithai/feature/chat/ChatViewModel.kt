package se.onemanstudio.playaroundwithai.feature.chat

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.GeminiModel
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GenerateContentUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetPromptHistoryUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetSuggestionsUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetFailedSyncCountUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetSyncStateUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.SavePromptUseCase
import se.onemanstudio.playaroundwithai.feature.chat.models.Attachment
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatError
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatUiState
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Date
import javax.inject.Inject

private const val SUBSCRIBE_TIMEOUT = 5000L
private const val JPEG_QUALITY = 100
private const val LOADING_MESSAGE_DURATION = 3000L

@Suppress("CanBeParameter", "LongParameterList")
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generateContentUseCase: GenerateContentUseCase,
    private val getSuggestionsUseCase: GetSuggestionsUseCase,
    private val getPromptHistoryUseCase: GetPromptHistoryUseCase,
    private val getSyncStateUseCase: GetSyncStateUseCase,
    private val getFailedSyncCountUseCase: GetFailedSyncCountUseCase,
    private val savePromptUseCase: SavePromptUseCase,
    private val application: Application
) : ViewModel() {
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    private val _isSuggestionsLoading = MutableStateFlow(false)
    val isSuggestionsLoading = _isSuggestionsLoading.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen = _isSheetOpen.asStateFlow()

    private val _selectedModel = MutableStateFlow(GeminiModel.FLASH_PREVIEW)
    val selectedModel = _selectedModel.asStateFlow()

    private val _syncFailureEvent = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val syncFailureEvent: SharedFlow<Int> = _syncFailureEvent.asSharedFlow()

    private val _currentLoadingMessage = MutableStateFlow("")
    val currentLoadingMessage = _currentLoadingMessage.asStateFlow()

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
        loadSuggestions()
        observeSyncFailures()
        startLoadingMessageCycle()
    }

    private fun observeSyncFailures() {
        viewModelScope.launch {
            getFailedSyncCountUseCase()
                .distinctUntilChanged()
                .filter { it > 0 }
                .collect { count ->
                    _syncFailureEvent.tryEmit(count)
                }
        }
    }

    fun selectModel(model: GeminiModel) {
        _selectedModel.value = model
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            _isSuggestionsLoading.value = true
            getSuggestionsUseCase(model = _selectedModel.value)
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

    fun generateContent(prompt: String, attachment: Attachment?) {
        _uiState.update { ChatUiState.Loading }

        viewModelScope.launch {
            // see if we have something attached
            val imageBytes = (attachment as? Attachment.Image)?.uri?.toByteArray()
            val analysisType = (attachment as? Attachment.Image)?.analysisType

            // read attached stuff
            val fileResult = (attachment as? Attachment.Document)?.uri?.let { extractFileContent(it) }

            // fail directly if something is off
            if (fileResult != null && fileResult.isFailure) {
                val error = when (val e = fileResult.exceptionOrNull()) {
                    is FileNotFoundException -> ChatError.FileNotFound
                    is SecurityException -> ChatError.Permission
                    else -> ChatError.FileRead
                }
                _uiState.update { ChatUiState.Error(error) }
                return@launch
            }

            val fileText = fileResult?.getOrNull()

            generateContentUseCase(
                prompt = prompt,
                imageBytes = imageBytes,
                fileText = fileText,
                analysisType = analysisType,
                model = _selectedModel.value,
            ).onSuccess { responseText ->
                _uiState.update {
                    ChatUiState.Success(responseText)
                }
                viewModelScope.launch {
                    val promptToSave = Prompt(
                        text = "Q: $prompt\nA: $responseText",
                        timestamp = Date(),
                        syncStatus = SyncStatus.Pending,
                        imageAttachment = imageBytes,
                        documentAttachment = fileText
                    )
                    savePromptUseCase(promptToSave)
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

    fun clearResponse() {
        _uiState.update { ChatUiState.Initial }
    }

    fun openHistorySheet() {
        _isSheetOpen.value = true
    }

    fun closeHistorySheet() {
        _isSheetOpen.value = false
    }

    private fun extractFileContent(uri: Uri): Result<String> {
        return try {
            val content = readTextFromUri(uri)
            Result.success(content)
        } catch (e: FileNotFoundException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SecurityException) {
            Result.failure(e)
        }
    }

    // Helper function that throws exceptions naturally
    @Throws(IOException::class, SecurityException::class)
    private fun readTextFromUri(uri: Uri): String {
        return application.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
                reader.readText()
            }
        } ?: throw FileNotFoundException("Could not open input stream for URI: $uri")
    }

    private fun Uri.toByteArray(): ByteArray? {
        return try {
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(application.contentResolver, this))
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos)
            bos.toByteArray()
        } catch (e: IOException) {
            Timber.d("Error decoding image: ${e.message}")
            null
        } catch (e: SecurityException) {
            Timber.d("Security exception decoding image: ${e.message}")
            null
        }
    }

    private fun startLoadingMessageCycle() {
        viewModelScope.launch {
            val messages = getLoadingMessages()
            var index = 0
            while (true) {
                _currentLoadingMessage.value = messages[index]
                delay(LOADING_MESSAGE_DURATION)
                index = (index + 1) % messages.size
            }
        }
    }

    private fun getLoadingMessages(): List<String> {
        return listOf(
            application.getString(R.string.loading_message_1),
            application.getString(R.string.loading_message_2),
            application.getString(R.string.loading_message_3),
            application.getString(R.string.loading_message_4),
            application.getString(R.string.loading_message_5)
        )
    }
}
