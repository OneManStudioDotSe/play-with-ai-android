package se.onemanstudio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import se.onemanstudio.playaroundwithai.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class OneManStudioApp : Application() {
    override fun onCreate() {
        super.onCreate()

        //"There are no Tree implementations installed by default because every time you log in production, a puppy dies."
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        Timber.d("Hello world")
    }
}
