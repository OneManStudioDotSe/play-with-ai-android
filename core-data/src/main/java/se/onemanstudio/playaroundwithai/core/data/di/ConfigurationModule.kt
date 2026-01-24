package se.onemanstudio.playaroundwithai.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor
import se.onemanstudio.playaroundwithai.core.data.BuildConfig
import se.onemanstudio.playaroundwithai.core.data.di.network.BaseUrl
import se.onemanstudio.playaroundwithai.core.data.di.network.GeminiApiKey
import se.onemanstudio.playaroundwithai.core.data.di.network.LoggingLevel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigurationModule {
    @Provides
    @Singleton
    @GeminiApiKey
    fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY

    @Provides
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Singleton
    @LoggingLevel
    fun provideLoggingLevel(): HttpLoggingInterceptor.Level {
        return if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}
