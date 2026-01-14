package se.onemanstudio.playaroundwithai.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.core.data.feature.auth.repository.AuthRepositoryImpl
import se.onemanstudio.playaroundwithai.core.data.feature.chat.repository.GeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.core.data.feature.map.repository.MapRepositoryImpl
import se.onemanstudio.playaroundwithai.core.domain.feature.auth.repository.AuthRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.map.repository.MapRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindGeminiDomainRepository(
        geminiRepositoryImpl: GeminiRepositoryImpl
    ): GeminiRepository

    @Binds
    abstract fun bindMapDomainRepository(
        mapRepositoryImpl: MapRepositoryImpl
    ): MapRepository

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
