package se.onemanstudio.playaroundwithai.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.BuildConfig
import se.onemanstudio.playaroundwithai.core.config.di.AppVersion
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @AppVersion
    fun provideAppVersion(): String = BuildConfig.VERSION_NAME
}
