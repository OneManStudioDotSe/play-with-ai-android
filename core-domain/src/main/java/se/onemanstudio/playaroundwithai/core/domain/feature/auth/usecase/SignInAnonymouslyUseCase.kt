package se.onemanstudio.playaroundwithai.core.domain.feature.auth.usecase

import se.onemanstudio.playaroundwithai.core.domain.feature.auth.model.AuthSession
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository.AuthRepository
import javax.inject.Inject

class SignInAnonymouslyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<AuthSession> {
        return authRepository.signInAnonymously()
    }
}
