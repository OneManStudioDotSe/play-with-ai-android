package se.onemanstudio.playaroundwithai.data.dream.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.onemanstudio.playaroundwithai.data.dream.data.local.dao.DreamsDao
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import se.onemanstudio.playaroundwithai.data.dream.data.mapper.toDomain as toDreamDomain
import se.onemanstudio.playaroundwithai.data.dream.data.mapper.toEntity as toDreamEntity

private const val LOG_PREVIEW_LENGTH = 50

@Singleton
class DreamRepositoryImpl @Inject constructor(
    private val dreamsDao: DreamsDao
) : DreamRepository {

    override suspend fun saveDream(dream: Dream): Long {
        Timber.d("DreamRepo - Saving dream: '${dream.description.take(LOG_PREVIEW_LENGTH)}...'")
        val insertedId = dreamsDao.insertDream(dream.toDreamEntity())
        Timber.d("DreamRepo - Dream saved to Room (id=$insertedId)")
        return insertedId
    }

    override fun getDreamHistory(): Flow<List<Dream>> =
        dreamsDao.getAllDreams().map { list ->
            Timber.v("DreamRepo - Dream history updated. We now have ${list.size} entries")
            list.map { it.toDreamDomain() }
        }

    override suspend fun getDreamById(id: Long): Dream? {
        return dreamsDao.getDreamById(id)?.toDreamDomain()
    }

    override suspend fun deleteDream(id: Long) {
        Timber.d("DreamRepo - Deleting dream id=$id")
        dreamsDao.deleteDream(id)
    }
}
