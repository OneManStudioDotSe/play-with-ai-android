package se.onemanstudio.playaroundwithai.data.dream.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.data.dream.data.local.entity.DreamEntity

@Dao
interface DreamsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDream(dream: DreamEntity): Long

    @Query("SELECT * FROM dreams ORDER BY timestamp DESC")
    fun getAllDreams(): Flow<List<DreamEntity>>

    @Query("SELECT * FROM dreams WHERE id = :id")
    suspend fun getDreamById(id: Long): DreamEntity?

    @Query("DELETE FROM dreams WHERE id = :id")
    suspend fun deleteDream(id: Long)
}
