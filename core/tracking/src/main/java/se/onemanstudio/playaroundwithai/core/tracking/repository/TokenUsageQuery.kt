package se.onemanstudio.playaroundwithai.core.tracking.repository

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.tracking.model.DailyTokenUsage

interface TokenUsageQuery {
    fun getWeeklyUsage(): Flow<List<DailyTokenUsage>>
}
