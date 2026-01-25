package se.onemanstudio.playaroundwithai.feature.chat

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GenerateContentUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetPromptHistoryUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.usecase.GetSuggestionsUseCase
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

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generateContentUseCase: GenerateContentUseCase,
    private val getSuggestionsUseCase: GetSuggestionsUseCase,
    private val getPromptHistoryUseCase: GetPromptHistoryUseCase,
    private val getSyncStateUseCase: GetSyncStateUseCase,
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

    val promptHistory: StateFlow<List<Prompt>> = getPromptHistoryUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadSuggestions()
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
                        listOf("Tell me a joke", "Explain Quantum Physics", "Roast my code")
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
                analysisType = analysisType
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.toByteArray()
        } catch (e: IOException) {
            Timber.d("Error decoding image: ${e.message}")
            null
        } catch (e: SecurityException) {
            Timber.d("Security exception decoding image: ${e.message}")
            null
        }
    }
}