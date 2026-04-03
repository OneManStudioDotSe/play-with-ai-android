package se.onemanstudio.playaroundwithai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import se.onemanstudio.playaroundwithai.data.chat.data.sync.SyncWorker
import se.onemanstudio.playaroundwithai.data.chat.domain.usecase.RetryPendingSyncsUseCase
import se.onemanstudio.playaroundwithai.data.chat.R
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class OneManStudioApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var retryPendingSyncsUseCase: RetryPendingSyncsUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val RETRY_SYNC_TIMEOUT_MS = 30_000L
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createNotificationChannel()
        retryFailedSyncs()

        Timber.d("OneManStudioApp started")
    }

    @Suppress("TooGenericExceptionCaught")
    private fun retryFailedSyncs() {
        applicationScope.launch {
            try {
                val completed = withTimeoutOrNull(RETRY_SYNC_TIMEOUT_MS) {
                    retryPendingSyncsUseCase()
                }
                if (completed != null) {
                    Timber.d("OneManStudioApp - Retried failed syncs on startup")
                } else {
                    Timber.w("OneManStudioApp - Sync retry timed out after ${RETRY_SYNC_TIMEOUT_MS}ms")
                }
            } catch (e: Exception) {
                Timber.e(e, "OneManStudioApp - Failed to retry syncs on startup")
            }
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.sync_notification_channel_name)
        val descriptionText = getString(R.string.sync_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(SyncWorker.SYNC_CHANNEL_FOR_DB, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
