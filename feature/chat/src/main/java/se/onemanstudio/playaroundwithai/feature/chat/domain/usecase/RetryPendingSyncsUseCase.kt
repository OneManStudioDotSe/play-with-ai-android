package se.onemanstudio.playaroundwithai.feature.chat.domain.usecase

import se.onemanstudio.playaroundwithai.feature.chat.domain.repository.PromptRepository
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

private const val MIN_RETRY_INTERVAL_MS = 5_000L

class RetryPendingSyncsUseCase @Inject constructor(
    private val repository: PromptRepository
) {
    private val lastRetryTimestamp = AtomicLong(0L)

    suspend operator fun invoke() {
        val now = System.currentTimeMillis()
        val lastRetry = lastRetryTimestamp.get()
        if (now - lastRetry < MIN_RETRY_INTERVAL_MS) {
            return
        }
        lastRetryTimestamp.set(now)
        repository.retryPendingSyncs()
    }
}
