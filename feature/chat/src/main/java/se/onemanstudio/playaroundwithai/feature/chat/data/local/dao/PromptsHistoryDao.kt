package se.onemanstudio.playaroundwithai.feature.chat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.feature.chat.data.local.entity.PromptEntity

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

    @Query("SELECT COUNT(*) FROM prompt_history WHERE syncStatus = :status")
    fun getCountBySyncStatus(status: String): Flow<Int>

    @Query("UPDATE prompt_history SET text = :text WHERE id = :id")
    suspend fun updatePromptText(id: Int, text: String)

    @Query("UPDATE prompt_history SET syncStatus = :newStatus WHERE syncStatus = :oldStatus")
    suspend fun updateAllSyncStatuses(oldStatus: String, newStatus: String)

    @Query("UPDATE prompt_history SET firestoreDocId = :docId WHERE id = :id")
    suspend fun updateFirestoreDocId(id: Int, docId: String)

    @Query("SELECT * FROM prompt_history WHERE id = :id")
    suspend fun getPromptById(id: Int): PromptEntity?

    @Query("UPDATE prompt_history SET syncStatus = :newStatus WHERE id = :id AND text = :expectedText")
    suspend fun markSyncedIfTextMatches(id: Int, expectedText: String, newStatus: String): Int
}
