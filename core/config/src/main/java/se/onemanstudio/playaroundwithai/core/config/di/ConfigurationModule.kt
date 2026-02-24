package se.onemanstudio.playaroundwithai.core.config.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor
import se.onemanstudio.playaroundwithai.core.config.BuildConfig
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
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
    @MapsApiKey
    fun provideMapsApiKey(): String = BuildConfig.MAPS_API_KEY

    @Provides
    @Singleton
    fun provideApiKeyAvailability(
        @GeminiApiKey geminiApiKey: String,
        @MapsApiKey mapsApiKey: String,
    ): ApiKeyAvailability = ApiKeyAvailability(
        isGeminiKeyAvailable = geminiApiKey.isNotBlank(),
        isMapsKeyAvailable = mapsApiKey.isNotBlank(),
    )

    @Provides
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Singleton
    @LoggingLevel
    fun provideLoggingLevel(): HttpLoggingInterceptor.Level {
        return if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}
