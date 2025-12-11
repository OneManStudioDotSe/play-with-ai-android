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
import se.onemanstudio.playaroundwithai.core.data.domain.model.Prompt
import se.onemanstudio.playaroundwithai.core.data.remote.gemini.GeminiRepository
import se.onemanstudio.playaroundwithai.feature.chat.models.Attachment
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatError
import se.onemanstudio.playaroundwithai.feature.chat.states.ChatUiState
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: GeminiRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen = _isSheetOpen.asStateFlow()

    val promptHistory: StateFlow<List<Prompt>> = repository.getPromptHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun generateContent(prompt: String, attachment: Attachment?) {
        _uiState.update { ChatUiState.Loading }

        viewModelScope.launch {
            // 1. Prepare Inputs
            val imageBitmap = (attachment as? Attachment.Image)?.uri?.toBitmap()
            val analysisType = (attachment as? Attachment.Image)?.analysisType

            // 2. Try to read file if it exists
            val fileResult = (attachment as? Attachment.Document)?.uri?.let { extractFileContent(it) }

            // 3. Fail immediately if file reading failed
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

            // 4. Proceed with API call
            repository.savePrompt(prompt)

            repository.generateContent(
                prompt = prompt,
                imageBitmap = imageBitmap,
                fileText = fileText,
                analysisType = analysisType
            ).onSuccess { response ->
                _uiState.update {
                    val text = response.extractText() ?: "No response text found."
                    ChatUiState.Success(text)
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

    private fun Uri.toBitmap(): Bitmap? {
        return try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(application.contentResolver, this))
        } catch (e: IOException) {
            Timber.d("Error decoding image: ${e.message}")
            null
        } catch (e: SecurityException) {
            Timber.d("Security exception decoding image: ${e.message}")
            null
        }
    }
}
