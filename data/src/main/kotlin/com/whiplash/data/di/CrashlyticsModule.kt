package com.whiplash.data.di

import com.whiplash.data.provider.CrashlyticsProviderImpl
import com.whiplash.domain.provider.CrashlyticsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashlyticsModule {

    @Binds
    @Singleton
    abstract fun bindCrashlyticsProvider(impl: CrashlyticsProviderImpl): CrashlyticsProvider

}