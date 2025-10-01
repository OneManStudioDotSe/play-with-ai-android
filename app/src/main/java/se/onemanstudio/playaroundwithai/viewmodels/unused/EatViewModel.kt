package se.onemanstudio.playaroundwithai.viewmodels.unused

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.states.UiState
import se.onemanstudio.playaroundwithai.data.unused.EatUseCase

class EatViewModel : ViewModel() {
    private val useCase = EatUseCase()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun performEatAction() {
        viewModelScope.launch {
            _state.value = UiState(isLoading = true)
            val result = useCase.execute()
            _state.value = UiState(result = result)
        }
    }
}