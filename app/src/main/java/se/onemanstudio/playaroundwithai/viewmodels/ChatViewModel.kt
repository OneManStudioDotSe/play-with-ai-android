package se.onemanstudio.playaroundwithai.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.data.AnalysisType
import se.onemanstudio.playaroundwithai.data.remote.gemini.GeminiRepository
import se.onemanstudio.playaroundwithai.data.local.PromptEntity
import javax.inject.Inject

sealed interface ChatUiState {
    data object Initial : ChatUiState
    data object Loading : ChatUiState
    data class Success(val outputText: String) : ChatUiState
    data class Error(val errorMessage: String) : ChatUiState
}

// Update the interface first
interface ChatViewModelInterface {
    // ... other properties
    fun generateContent(prompt: String, imageUri: Uri?, analysisType: AnalysisType)
    fun clearResponse()
    // ... other functions
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: GeminiRepository,
    @ApplicationContext private val context: Context // Inject context
) : ViewModel(), ChatViewModelInterface {

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

    override fun generateContent(prompt: String, imageUri: Uri?, analysisType: AnalysisType) {
        if (prompt.isBlank() && imageUri == null) return

        viewModelScope.launch { repository.savePrompt(prompt) }
        _uiState.update { ChatUiState.Loading }

        viewModelScope.launch {
            val bitmap = imageUri?.toBitmap()
            repository.generateContent(prompt, bitmap, analysisType).onSuccess { response ->

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

    // Helper to convert Uri to Bitmap
    private fun Uri.toBitmap(): Bitmap? {
        return try {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun clearResponse() {
        _uiState.update { ChatUiState.Initial }
    }

    fun openHistorySheet() {
        _isSheetOpen.value = true
    }

    fun closeHistorySheet() {
        _isSheetOpen.value = false
    }
}
