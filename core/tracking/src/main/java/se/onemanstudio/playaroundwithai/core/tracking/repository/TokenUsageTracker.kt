package se.onemanstudio.playaroundwithai.core.tracking.repository

import kotlinx.coroutines.flow.SharedFlow
import se.onemanstudio.playaroundwithai.core.network.dto.UsageMetadata
import se.onemanstudio.playaroundwithai.core.tracking.model.TokenUsageEvent

interface TokenUsageTracker {
    val lastUsageEvent: SharedFlow<TokenUsageEvent>
    suspend fun record(feature: String, usageMetadata: UsageMetadata?)
}
