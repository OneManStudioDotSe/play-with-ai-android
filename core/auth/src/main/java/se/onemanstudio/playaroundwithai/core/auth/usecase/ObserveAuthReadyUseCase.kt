package se.onemanstudio.playaroundwithai.core.auth.usecase

import kotlinx.coroutines.flow.StateFlow
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import javax.inject.Inject

class ObserveAuthReadyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<Boolean> = authRepository.authReady
}
