package se.onemanstudio.playaroundwithai.viewmodels

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
import se.onemanstudio.playaroundwithai.data.gemini.GeminiRepository
import se.onemanstudio.playaroundwithai.data.local.PromptEntity
import javax.inject.Inject

sealed interface ChatUiState {
    data object Initial : ChatUiState
    data object Loading : ChatUiState
    data class Success(val outputText: String) : ChatUiState
    data class Error(val errorMessage: String) : ChatUiState
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: GeminiRepository
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

    fun generateContent(prompt: String) {
        if (prompt.isBlank()) return

        // Save the prompt before making the API call
        viewModelScope.launch {
            repository.savePrompt(prompt)
        }

        _uiState.update { ChatUiState.Loading }

        viewModelScope.launch {
            repository.generateContent(prompt).onSuccess { response ->
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

    fun openHistorySheet() {
        _isSheetOpen.value = true
    }

    fun closeHistorySheet() {
        _isSheetOpen.value = false
    }
}
