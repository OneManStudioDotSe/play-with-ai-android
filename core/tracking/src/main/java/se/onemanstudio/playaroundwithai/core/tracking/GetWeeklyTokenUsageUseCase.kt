package se.onemanstudio.playaroundwithai.core.tracking

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeeklyTokenUsageUseCase @Inject constructor(
    private val tokenUsageQuery: TokenUsageQuery,
) {
    operator fun invoke(): Flow<List<DailyTokenUsage>> = tokenUsageQuery.getWeeklyUsage()
}
