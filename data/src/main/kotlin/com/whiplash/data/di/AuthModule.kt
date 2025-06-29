package com.whiplash.data.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.usecase.login.GetCurrentUserUseCase
import com.whiplash.domain.usecase.login.GetGoogleSignInIntentUseCase
import com.whiplash.domain.usecase.login.SignInWithGoogleUseCase
import com.whiplash.domain.usecase.login.SignOutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
        CredentialManager.create(context)

    @Provides
    @Singleton
    fun provideSignInWithGoogleUseCase(repository: GoogleAuthRepository): SignInWithGoogleUseCase =
        SignInWithGoogleUseCase(repository)

    @Provides
    @Singleton
    fun provideGetGoogleSignInIntentUseCase(repository: GoogleAuthRepository): GetGoogleSignInIntentUseCase =
        GetGoogleSignInIntentUseCase(repository)

    @Provides
    @Singleton
    fun provideSignOutUseCase(repository: GoogleAuthRepository): SignOutUseCase =
        SignOutUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(repository: GoogleAuthRepository): GetCurrentUserUseCase =
        GetCurrentUserUseCase(repository)
}