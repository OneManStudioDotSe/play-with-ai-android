package se.onemanstudio.playaroundwithai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.database.entity.TokenUsageEntity
import se.onemanstudio.playaroundwithai.core.database.model.DailyUsageRow

@Dao
interface TokenUsageDao {
    @Insert
    suspend fun insert(entity: TokenUsageEntity)

    @Query(
        """
        SELECT dateMillis,
               SUM(totalTokens) AS totalTokens,
               SUM(promptTokens) AS promptTokens,
               SUM(candidateTokens) AS candidateTokens,
               COUNT(*) AS callCount
        FROM token_usage
        WHERE dateMillis >= :startDateMillis
        GROUP BY dateMillis
        ORDER BY dateMillis ASC
        """
    )
    fun getDailyUsageSince(startDateMillis: Long): Flow<List<DailyUsageRow>>
}
