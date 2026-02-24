package se.onemanstudio.playaroundwithai.feature.maps.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.feature.maps.data.api.FakeMapApiService
import se.onemanstudio.playaroundwithai.feature.maps.data.api.MapApiService
import se.onemanstudio.playaroundwithai.feature.maps.data.repository.MapGeminiRepositoryImpl
import se.onemanstudio.playaroundwithai.feature.maps.data.repository.MapRepositoryImpl
import se.onemanstudio.playaroundwithai.feature.maps.domain.repository.MapGeminiRepository
import se.onemanstudio.playaroundwithai.feature.maps.domain.repository.MapRepository
import se.onemanstudio.playaroundwithai.feature.maps.util.ConnectivityNetworkMonitor
import se.onemanstudio.playaroundwithai.feature.maps.util.NetworkMonitor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {

    @Binds
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindMapApiService(impl: FakeMapApiService): MapApiService

    @Binds
    @Singleton
    abstract fun bindMapRepository(impl: MapRepositoryImpl): MapRepository

    @Binds
    @Singleton
    abstract fun bindMapGeminiRepository(impl: MapGeminiRepositoryImpl): MapGeminiRepository
}
