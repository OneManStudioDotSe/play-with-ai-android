package se.onemanstudio.playaroundwithai.core.tracking.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.onemanstudio.playaroundwithai.core.tracking.TokenUsageQuery
import se.onemanstudio.playaroundwithai.core.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.core.tracking.TokenUsageTrackerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {

    @Binds
    @Singleton
    abstract fun bindTokenUsageTracker(impl: TokenUsageTrackerImpl): TokenUsageTracker

    @Binds
    @Singleton
    abstract fun bindTokenUsageQuery(impl: TokenUsageTrackerImpl): TokenUsageQuery
}
