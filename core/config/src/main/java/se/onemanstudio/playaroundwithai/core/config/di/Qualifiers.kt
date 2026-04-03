package se.onemanstudio.playaroundwithai.core.config.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiApiKey

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoggingLevel

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MapsApiKey
