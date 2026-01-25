package se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.firebase

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import se.onemanstudio.playaroundwithai.core.data.R
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.FirestoreDataSource
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val promptsDao: PromptsHistoryDao,
    private val firestoreDataSource: FirestoreDataSource
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("Syncing pending prompts to Firestore...")

        val pendingPrompts = promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name)
        if (pendingPrompts.isEmpty()) {
            return Result.success()
        }

        setForeground(createForegroundInfo())

        var allSuccessful = true
        pendingPrompts.forEach { entity ->
            val syncResult = firestoreDataSource.savePrompt(entity.text, entity.timestamp)
            if (syncResult.isSuccess) {
                promptsDao.updateSyncStatus(entity.id, SyncStatus.Synced.name)
            } else {
                allSuccessful = false
                Timber.e(syncResult.exceptionOrNull(), "Failed to sync prompt: ${entity.id}")
            }
        }

        return if (allSuccessful) Result.success() else Result.retry()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val title = context.getString(R.string.sync_notification_title)
        val content = context.getString(R.string.sync_notification_content)

        val notification = NotificationCompat.Builder(context, "sync_channel")
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    companion object {
        private const val NOTIFICATION_ID = 101
    }
}
