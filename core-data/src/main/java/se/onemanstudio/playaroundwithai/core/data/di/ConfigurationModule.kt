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
import se.onemanstudio.playaroundwithai.core.data.di.network.MapsApiKey
import se.onemanstudio.playaroundwithai.core.domain.feature.config.model.ApiKeyAvailability
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
