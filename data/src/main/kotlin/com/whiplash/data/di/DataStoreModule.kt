package com.whiplash.data.di

import android.content.Context
import com.whiplash.data.datastore.OnboardingDataStore
import com.whiplash.data.datastore.TokenDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context
    ): TokenDataStore = TokenDataStore(context)

    @Provides
    @Singleton
    fun provideOnboardingDataStore(
        @ApplicationContext context: Context
    ): OnboardingDataStore = OnboardingDataStore(context)

}