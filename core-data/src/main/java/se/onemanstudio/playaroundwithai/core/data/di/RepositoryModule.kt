package se.onemanstudio.playaroundwithai.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.core.data.feature.auth.repository.AuthRepositoryImpl
import se.onemanstudio.playaroundwithai.core.data.feature.chat.repository.GeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.core.data.feature.chat.repository.PromptRepositoryImpl
import se.onemanstudio.playaroundwithai.core.data.feature.map.repository.MapRepositoryImpl
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.PromptRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.map.repository.MapRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPromptRepository(impl: PromptRepositoryImpl): PromptRepository

    @Binds
    @Singleton
    abstract fun bindGeminiDomainRepository(geminiRepositoryImpl: GeminiRepositoryImpl): GeminiRepository

    @Binds
    @Singleton
    abstract fun bindMapDomainRepository(mapRepositoryImpl: MapRepositoryImpl): MapRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository
}
