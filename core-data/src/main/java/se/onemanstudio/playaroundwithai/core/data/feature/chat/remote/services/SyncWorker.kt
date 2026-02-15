package se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.services

import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.google.firebase.auth.FirebaseAuth
import se.onemanstudio.playaroundwithai.core.data.R
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.api.FirestoreDataSource
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import timber.log.Timber

private const val SYNC_CHANNEL_FOR_DB = "sync_channel"

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val promptsDao: PromptsHistoryDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(context, params) {

    @Suppress("ReturnCount")
    override suspend fun doWork(): Result {
        Timber.d("SyncWorker — Started (attempt ${runAttemptCount + 1}/$MAX_RETRY_COUNT)...")

        if (firebaseAuth.currentUser == null) {
            Timber.w("SyncWorker - User is not authenticated. Skipping sync")
            return Result.failure()
        }

        val pendingPrompts = promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name)
        if (pendingPrompts.isEmpty()) {
            Timber.d("SyncWorker - No pending prompts found, all good!")
            return Result.success()
        }

        Timber.d("SyncWorker - I found ${pendingPrompts.size} pending prompts to sync. Let's do it")
        setForeground(createForegroundInfo())

        var allSuccessful = true
        pendingPrompts.forEach { entity ->
            Timber.d("SyncWorker - Syncing prompt with id ${entity.id} to Firestore...")
            val syncResult = firestoreDataSource.savePrompt(entity.text, entity.timestamp)
            if (syncResult.isSuccess) {
                promptsDao.updateSyncStatus(entity.id, SyncStatus.Synced.name)
                Timber.d("SyncWorker - prompt with id ${entity.id} was synced successfully!")
            } else {
                allSuccessful = false
                Timber.e(syncResult.exceptionOrNull(), "SyncWorker - failed to sync prompt with id ${entity.id} :/")
            }
        }

        return when {
            allSuccessful -> {
                Timber.d("SyncWorker completed — all ${pendingPrompts.size} prompt(s) synced successfully")
                Result.success()
            }
            runAttemptCount < MAX_RETRY_COUNT - 1 -> {
                Timber.w("SyncWorker - Attempt ${runAttemptCount + 1} failed, will retry")
                Result.retry()
            }
            else -> {
                Timber.e("SyncWorker - All $MAX_RETRY_COUNT attempts exhausted. Marking remaining prompts as Failed")
                val stillPending = promptsDao.getPromptsBySyncStatus(SyncStatus.Pending.name)
                stillPending.forEach { entity ->
                    promptsDao.updateSyncStatus(entity.id, SyncStatus.Failed.name)
                }
                showFailureNotification(stillPending.size)
                Result.failure()
            }
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val title = context.getString(R.string.sync_notification_title)
        val content = context.getString(R.string.sync_notification_content)

        val notification = NotificationCompat.Builder(context, SYNC_CHANNEL_FOR_DB)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private fun showFailureNotification(failedCount: Int) {
        val notification = NotificationCompat.Builder(context, SYNC_CHANNEL_FOR_DB)
            .setContentTitle(context.getString(R.string.sync_failure_notification_title))
            .setContentText(context.getString(R.string.sync_failure_notification_content, failedCount))
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(FAILURE_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val FAILURE_NOTIFICATION_ID = 102
        private const val MAX_RETRY_COUNT = 3
    }
}
