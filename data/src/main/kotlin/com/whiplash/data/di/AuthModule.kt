package com.whiplash.data.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.whiplash.domain.repository.login.GoogleAuthRepository
import com.whiplash.domain.repository.login.KakaoAuthRepository
import com.whiplash.domain.usecase.login.google.GetCurrentUserUseCase
import com.whiplash.domain.usecase.login.google.GetGoogleSignInIntentUseCase
import com.whiplash.domain.usecase.login.google.HandleGoogleSignInResultUseCase
import com.whiplash.domain.usecase.login.google.SignInWithGoogleUseCase
import com.whiplash.domain.usecase.login.google.SignOutUseCase
import com.whiplash.domain.usecase.login.kakao.GetCurrentKakaoUserUseCase
import com.whiplash.domain.usecase.login.kakao.SignInWithKakaoUseCase
import com.whiplash.domain.usecase.login.kakao.SignOutKakaoUseCase
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

    @Provides
    @Singleton
    fun provideHandleGoogleSignInResultUseCase(repository: GoogleAuthRepository): HandleGoogleSignInResultUseCase =
        HandleGoogleSignInResultUseCase(repository)

    @Provides
    @Singleton
    fun provideSignInWithKakaoUseCase(repository: KakaoAuthRepository): SignInWithKakaoUseCase =
        SignInWithKakaoUseCase(repository)

    @Provides
    @Singleton
    fun provideSignOutKakaoUseCase(repository: KakaoAuthRepository): SignOutKakaoUseCase =
        SignOutKakaoUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCurrentKakaoUserUseCase(repository: KakaoAuthRepository): GetCurrentKakaoUserUseCase =
        GetCurrentKakaoUserUseCase(repository)
}