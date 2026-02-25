package se.onemanstudio.playaroundwithai.data.chat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.data.chat.data.local.entity.TokenUsageEntity

data class DailyUsageRow(
    val dateMillis: Long,
    val totalTokens: Long,
    val promptTokens: Long,
    val candidateTokens: Long,
    val callCount: Int,
)

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
