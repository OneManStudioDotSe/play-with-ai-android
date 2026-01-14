package se.onemanstudio.playaroundwithai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.usecase.SignInAnonymouslyUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase
) : ViewModel() {

    init {
        signIn()
    }

    private fun signIn() {
        viewModelScope.launch {
            signInAnonymouslyUseCase()
        }
    }
}
