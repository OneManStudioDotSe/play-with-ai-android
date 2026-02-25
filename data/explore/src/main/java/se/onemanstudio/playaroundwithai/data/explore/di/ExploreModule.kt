package se.onemanstudio.playaroundwithai.data.explore.di

import android.content.Context
import android.net.ConnectivityManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.data.explore.data.api.FakeExploreItemsService
import se.onemanstudio.playaroundwithai.data.explore.data.api.ExploreApiService
import se.onemanstudio.playaroundwithai.data.explore.data.repository.ExploreSuggestionsRepositoryImpl
import se.onemanstudio.playaroundwithai.data.explore.data.repository.ExplorePointsRepositoryImpl
import se.onemanstudio.playaroundwithai.data.explore.domain.repository.ExploreSuggestionsRepository
import se.onemanstudio.playaroundwithai.data.explore.domain.repository.ExplorePointsRepository
import se.onemanstudio.playaroundwithai.data.explore.util.ConnectivityNetworkMonitor
import se.onemanstudio.playaroundwithai.data.explore.util.NetworkMonitor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExploreModule {

    @Binds
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindExploreApiService(impl: FakeExploreItemsService): ExploreApiService

    @Binds
    @Singleton
    abstract fun bindExploreRepository(impl: ExplorePointsRepositoryImpl): ExplorePointsRepository

    @Binds
    @Singleton
    abstract fun bindExploreSuggestionsRepository(impl: ExploreSuggestionsRepositoryImpl): ExploreSuggestionsRepository

    companion object {
        @Provides
        fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
            context.getSystemService(ConnectivityManager::class.java)
    }
}
