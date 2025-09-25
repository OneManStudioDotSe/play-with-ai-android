package se.onemanstudio.playaroundwithai.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.data.gemini.GeminiRepository
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

    fun generateContent(prompt: String) {
        if (prompt.isBlank()) return

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
}
