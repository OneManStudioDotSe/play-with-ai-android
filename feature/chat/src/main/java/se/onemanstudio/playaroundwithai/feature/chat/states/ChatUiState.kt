package se.onemanstudio.playaroundwithai.feature.chat.states

import androidx.compose.runtime.Immutable
import se.onemanstudio.playaroundwithai.data.chat.domain.model.Prompt

@Immutable
sealed interface ChatUiState {
    data object Initial : ChatUiState
    data object Loading : ChatUiState
    data class Success(val outputText: String) : ChatUiState
    data class Error(val error: ChatError) : ChatUiState
}

@Immutable
sealed interface ChatError {
    data object ApiKeyMissing : ChatError
    data object NetworkMissing : ChatError
    data object Permission : ChatError
    data object FileNotFound : ChatError
    data object FileRead : ChatError
    data class Unknown(val message: String?) : ChatError
}

@Immutable
data class ChatScreenState(
    val chatState: ChatUiState = ChatUiState.Initial,
    val suggestions: List<String> = emptyList(),
    val useFallbackSuggestions: Boolean = false,
    val isSuggestionsLoading: Boolean = false,
    val promptHistory: List<Prompt> = emptyList(),
    val isSyncing: Boolean = false,
)
