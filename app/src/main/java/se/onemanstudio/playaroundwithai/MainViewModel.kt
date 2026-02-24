package se.onemanstudio.playaroundwithai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.auth.usecase.SignInAnonymouslyUseCase
import timber.log.Timber
import javax.inject.Inject

private const val MAX_AUTH_ATTEMPTS = 3

@HiltViewModel
class MainViewModel @Inject constructor(
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase
) : ViewModel() {

    private val _authError = MutableStateFlow(false)
    val authError: StateFlow<Boolean> = _authError.asStateFlow()

    private val _authRetriesExhausted = MutableStateFlow(false)
    val authRetriesExhausted: StateFlow<Boolean> = _authRetriesExhausted.asStateFlow()

    private var authAttemptCount = 0

    init {
        signIn()
    }

    private fun signIn() {
        viewModelScope.launch {
            authAttemptCount++
            signInAnonymouslyUseCase()
                .onSuccess { session ->
                    Timber.d("Auth - Session established for user ${session.userId} from @ ${session.authProvider}")
                    _authError.update { false }
                    _authRetriesExhausted.update { false }
                    authAttemptCount = 0
                }
                .onFailure { e ->
                    Timber.e(e, "Auth - Anonymous sign-in failed (attempt $authAttemptCount/$MAX_AUTH_ATTEMPTS)")
                    _authError.update { true }
                    if (authAttemptCount >= MAX_AUTH_ATTEMPTS) {
                        _authRetriesExhausted.update { true }
                    }
                }
        }
    }

    fun retryAuth() {
        if (_authRetriesExhausted.value) return

        _authError.update { false }
        signIn()
    }

}
