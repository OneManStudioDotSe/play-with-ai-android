package se.onemanstudio.playaroundwithai.data.dream.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.core.database.dao.DreamsDao
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import se.onemanstudio.playaroundwithai.data.dream.data.mapper.toDomain as toDreamDomain
import se.onemanstudio.playaroundwithai.data.dream.data.mapper.toEntity as toDreamEntity

private const val LOG_PREVIEW_LENGTH = 50
private const val DREAM_IMAGES_DIR = "dream_images"

@Singleton
class DreamRepositoryImpl @Inject constructor(
    private val dreamsDao: DreamsDao,
    @param:ApplicationContext private val context: Context,
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
        val dream = dreamsDao.getDreamById(id)
        // Delete DB record first so the dream disappears from UI even if file deletion fails
        dreamsDao.deleteDream(id)
        dream?.imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
                Timber.d("DreamRepo - Deleted image file: $path")
            }
        }
    }

    override suspend fun saveDreamImage(dreamId: Long, imageBytes: ByteArray, mimeType: String, artistName: String): String =
        withContext(Dispatchers.IO) {
            val ext = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                else -> "jpg"
            }
            val dir = File(context.filesDir, DREAM_IMAGES_DIR)
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "dream_${dreamId}.$ext")
            file.writeBytes(imageBytes)
            Timber.d("DreamRepo - Saved dream image to ${file.absolutePath} (${imageBytes.size} bytes)")

            dreamsDao.updateDreamImage(dreamId, file.absolutePath, artistName)
            file.absolutePath
        }
}
