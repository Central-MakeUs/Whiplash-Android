package com.whiplash.data.di

import com.whiplash.data.repository.login.GoogleAuthRepositoryImpl
import com.whiplash.domain.repository.login.GoogleAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindGoogleAuthRepository(impl: GoogleAuthRepositoryImpl): GoogleAuthRepository

}