package se.onemanstudio.playaroundwithai.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.BuildConfig
import se.onemanstudio.playaroundwithai.core.data.di.BaseUrl
import se.onemanstudio.playaroundwithai.core.data.di.GeminiApiKey
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
}