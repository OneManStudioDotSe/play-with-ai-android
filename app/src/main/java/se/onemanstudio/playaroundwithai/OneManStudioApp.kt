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
import se.onemanstudio.playaroundwithai.feature.chat.domain.usecase.RetryPendingSyncsUseCase
import se.onemanstudio.playaroundwithai.feature.chat.R
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class OneManStudioApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var retryPendingSyncsUseCase: RetryPendingSyncsUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    @SuppressWarnings("TooGenericExceptionCaught")
    private fun retryFailedSyncs() {
        applicationScope.launch {
            try {
                retryPendingSyncsUseCase()
                Timber.d("OneManStudioApp - Retried failed syncs on startup")
            } catch (e: Exception) {
                Timber.e(e, "OneManStudioApp - Failed to retry syncs on startup")
            }
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.sync_notification_channel_name)
        val descriptionText = getString(R.string.sync_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel("sync_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
