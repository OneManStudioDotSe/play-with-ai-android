package se.onemanstudio.playaroundwithai.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.states.ScreenState
import se.onemanstudio.playaroundwithai.data.SeeUseCase

class SeeViewModel : ViewModel() {
    private val useCase = SeeUseCase()

    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    fun performSeeAction() {
        viewModelScope.launch {
            _state.value = ScreenState(isLoading = true)
            val result = useCase.execute()
            _state.value = ScreenState(result = result)
        }
    }
}

