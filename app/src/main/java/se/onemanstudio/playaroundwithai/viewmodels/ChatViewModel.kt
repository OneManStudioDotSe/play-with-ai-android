package se.onemanstudio.playaroundwithai.viewmodels

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
import se.onemanstudio.playaroundwithai.data.local.PromptEntity
import se.onemanstudio.playaroundwithai.data.remote.gemini.GeminiRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: GeminiRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState = _uiState.asStateFlow()

    // State for the bottom sheet visibility
    private val _isSheetOpen = MutableStateFlow(false)
    val isSheetOpen = _isSheetOpen.asStateFlow()

    // State for the prompt history
    val promptHistory: StateFlow<List<PromptEntity>> = repository.getPromptHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun generateContent(prompt: String, attachment: Attachment?) {
        _uiState.update { ChatUiState.Loading }

        viewModelScope.launch {
            val imageBitmap = (attachment as? Attachment.Image)?.uri?.toBitmap()
            val fileText = (attachment as? Attachment.Document)?.uri?.let { extractTextFromFile(it) }
            val analysisType = (attachment as? Attachment.Image)?.analysisType

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
                _uiState.update {
                    ChatUiState.Error(exception.localizedMessage ?: "An unknown error occurred")
                }
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

    // Helper function to read a URI's content into a string
    private fun extractTextFromFile(uri: Uri): String {
        val stringBuilder = StringBuilder()
        try {
            application.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append("\n")
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error reading file content."
        }
        return stringBuilder.toString()
    }

    // Helper to convert Uri to Bitmap
    private fun Uri.toBitmap(): Bitmap? {
        return try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(application.contentResolver, this))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
