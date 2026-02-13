package se.onemanstudio.playaroundwithai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.usecase.SignInAnonymouslyUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase
) : ViewModel() {

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    init {
        signIn()
    }

    private fun signIn() {
        viewModelScope.launch {
            signInAnonymouslyUseCase()
                .onSuccess { session ->
                    Timber.d(
                        "Auth - Session established: userId=%s, isNewUser=%s, accountAge=%d days, provider=%s",
                        session.userId, session.isNewUser, session.accountAgeDays, session.authProvider
                    )
                    _authError.value = false
                }
                .onFailure { e ->
                    Timber.e(e, "Anonymous sign-in failed")
                    _authError.value = true
                }
        }
    }

    fun retryAuth() {
        _authError.value = false
        signIn()
    }
}
