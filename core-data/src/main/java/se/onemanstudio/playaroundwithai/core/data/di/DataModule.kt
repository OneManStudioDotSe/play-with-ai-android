package se.onemanstudio.playaroundwithai.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.core.data.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.data.feature.chat.repository.GeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.core.data.feature.map.repository.MapRepository
import se.onemanstudio.playaroundwithai.core.data.feature.map.repository.MapRepositoryImpl
import se.onemanstudio.playaroundwithai.core.domain.repository.GeminiDomainRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindMapRepository(
        mapRepositoryImpl: MapRepositoryImpl
    ): MapRepository

    @Binds
    abstract fun bindGeminiRepository(
        geminiRepositoryImpl: GeminiRepositoryImpl
    ): GeminiRepository

    @Binds
    abstract fun bindGeminiDomainRepository(
        geminiRepositoryImpl: GeminiRepositoryImpl
    ): GeminiDomainRepository
}
