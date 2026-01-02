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
    suspend fun insertPrompt(prompt: PromptEntity)

    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    fun getPromptHistory(): Flow<List<PromptEntity>>
}
