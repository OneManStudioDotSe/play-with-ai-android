package se.onemanstudio.playaroundwithai.core.domain.feature.auth.usecase

import kotlinx.coroutines.flow.StateFlow
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository.AuthRepository
import javax.inject.Inject

class ObserveAuthReadyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<Boolean> = authRepository.authReady
}
