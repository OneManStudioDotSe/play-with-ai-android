package se.onemanstudio.playaroundwithai.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity)

    @Query("SELECT * FROM prompt_history ORDER BY timestamp DESC")
    fun getPromptHistory(): Flow<List<PromptEntity>>
}
