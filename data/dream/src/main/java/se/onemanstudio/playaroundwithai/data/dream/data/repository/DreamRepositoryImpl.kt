package se.onemanstudio.playaroundwithai.data.dream.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.core.database.dao.DreamsDao
import se.onemanstudio.playaroundwithai.data.dream.di.DreamImagesDir
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import se.onemanstudio.playaroundwithai.data.dream.data.mapper.toDomain as toDreamDomain
import se.onemanstudio.playaroundwithai.data.dream.data.mapper.toEntity as toDreamEntity

@Singleton
class DreamRepositoryImpl @Inject constructor(
    private val dreamsDao: DreamsDao,
    @param:DreamImagesDir private val dreamImagesDir: File,
) : DreamRepository {

    override suspend fun saveDream(dream: Dream): Long {
        val insertedId = dreamsDao.insertDream(dream.toDreamEntity())

        return insertedId
    }

    override fun getDreamHistory(): Flow<List<Dream>> =
        dreamsDao.getAllDreams().map { list ->
            list.map { it.toDreamDomain() }
        }

    override suspend fun getDreamById(id: Long): Dream? {
        return dreamsDao.getDreamById(id)?.toDreamDomain()
    }

    override suspend fun deleteDream(id: Long) {
        val dream = dreamsDao.getDreamById(id)

        // Delete DB record first so the dream disappears from UI even if file deletion fails
        dreamsDao.deleteDream(id)
        dream?.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                if (!file.delete()) Timber.w("DreamRepo - Failed to delete image file: $path")
            }
        }
    }

    override suspend fun saveDreamImage(
        dreamId: Long,
        imageBytes: ByteArray,
        mimeType: String,
        artistName: String
    ): String =
        withContext(Dispatchers.IO) {
            val ext = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }
            if (!dreamImagesDir.exists()) dreamImagesDir.mkdirs()

            val file = File(dreamImagesDir, "dream_${dreamId}.$ext")
            file.writeBytes(imageBytes)

            @Suppress("TooGenericExceptionCaught") // cleanup-and-rethrow: unknown Room exception type
            try {
                dreamsDao.updateDreamImage(dreamId, file.absolutePath, artistName)
            } catch (e: Exception) {
                Timber.e(e, "DreamRepo - Failed to update DB after writing image; deleting orphaned file")
                file.delete()
                throw e
            }

            file.absolutePath
        }
}
