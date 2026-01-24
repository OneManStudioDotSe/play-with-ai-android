package se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity

@Dao
interface PromptsHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePrompt(prompt: PromptEntity): Long

    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    fun getPromptHistory(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompt_history WHERE syncStatus = :status")
    suspend fun getPromptsBySyncStatus(status: String): List<PromptEntity>

    @Query("UPDATE prompt_history SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: String)
}
