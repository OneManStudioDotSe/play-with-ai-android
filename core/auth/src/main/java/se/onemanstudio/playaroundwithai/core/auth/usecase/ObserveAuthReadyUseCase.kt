package se.onemanstudio.playaroundwithai.core.auth.usecase

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import se.onemanstudio.playaroundwithai.core.auth.repository.AuthRepository
import javax.inject.Inject

private const val AUTH_READY_TIMEOUT_MS = 10_000L

class ObserveAuthReadyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<Boolean> = authRepository.authReady

    suspend fun awaitReady(): Boolean {
        return try {
            withTimeout(AUTH_READY_TIMEOUT_MS) {
                authRepository.authReady.first { it }
            }
        } catch (@Suppress("SwallowedException") e: TimeoutCancellationException) {
            false
        }
    }
}
