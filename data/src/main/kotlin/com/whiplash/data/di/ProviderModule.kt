package com.whiplash.data.di

import com.whiplash.data.provider.TokenProviderImpl
import com.whiplash.domain.provider.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: TokenProviderImpl): TokenProvider

}