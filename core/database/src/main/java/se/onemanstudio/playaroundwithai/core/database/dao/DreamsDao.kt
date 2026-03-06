package se.onemanstudio.playaroundwithai.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.core.database.entity.DreamEntity

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

    @Query("UPDATE dreams SET imagePath = :imagePath, artistName = :artistName WHERE id = :dreamId")
    suspend fun updateDreamImage(dreamId: Long, imagePath: String, artistName: String)
}
