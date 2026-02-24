package se.onemanstudio.playaroundwithai.data.maps.di

import android.content.Context
import android.net.ConnectivityManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.data.maps.data.api.FakeMapApiService
import se.onemanstudio.playaroundwithai.data.maps.data.api.MapApiService
import se.onemanstudio.playaroundwithai.data.maps.data.repository.MapSuggestionsRepositoryImpl
import se.onemanstudio.playaroundwithai.data.maps.data.repository.MapPointsRepositoryImpl
import se.onemanstudio.playaroundwithai.data.maps.domain.repository.MapSuggestionsRepository
import se.onemanstudio.playaroundwithai.data.maps.domain.repository.MapPointsRepository
import se.onemanstudio.playaroundwithai.data.maps.util.ConnectivityNetworkMonitor
import se.onemanstudio.playaroundwithai.data.maps.util.NetworkMonitor
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
    abstract fun bindMapRepository(impl: MapPointsRepositoryImpl): MapPointsRepository

    @Binds
    @Singleton
    abstract fun bindMapGeminiRepository(impl: MapSuggestionsRepositoryImpl): MapSuggestionsRepository

    companion object {
        @Provides
        fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
            context.getSystemService(ConnectivityManager::class.java)
    }
}
