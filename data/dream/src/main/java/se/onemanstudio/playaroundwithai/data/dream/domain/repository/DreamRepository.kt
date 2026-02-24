package se.onemanstudio.playaroundwithai.data.dream.domain.repository

import kotlinx.coroutines.flow.Flow
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream

interface DreamRepository {
    suspend fun saveDream(dream: Dream): Long
    fun getDreamHistory(): Flow<List<Dream>>
    suspend fun getDreamById(id: Long): Dream?
    suspend fun deleteDream(id: Long)
}
