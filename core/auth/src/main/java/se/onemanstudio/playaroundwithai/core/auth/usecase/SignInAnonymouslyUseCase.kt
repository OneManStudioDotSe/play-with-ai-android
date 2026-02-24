package se.onemanstudio.playaroundwithai.core.auth.usecase

import se.onemanstudio.playaroundwithai.core.auth.model.AuthSession
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import javax.inject.Inject

class SignInAnonymouslyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<AuthSession> {
        return authRepository.signInAnonymously().also { result ->
            result.onSuccess { session ->
                require(session.userId.isNotBlank()) { "Auth session returned a blank userId" }
            }
        }
    }
}
