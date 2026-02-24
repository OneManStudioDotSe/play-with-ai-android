package se.onemanstudio.playaroundwithai.core.network.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.onemanstudio.playaroundwithai.core.config.di.BaseUrl
import se.onemanstudio.playaroundwithai.core.config.di.LoggingLevel
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.interceptor.AuthenticationInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val TIMEOUT_SECONDS = 30L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthenticationInterceptor,
        @LoggingLevel loggingLevel: HttpLoggingInterceptor.Level
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = loggingLevel
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideRetrofit(@BaseUrl baseUrl: String, okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(retrofit: Retrofit): GeminiApiService {
        return retrofit.create(GeminiApiService::class.java)
    }
}
