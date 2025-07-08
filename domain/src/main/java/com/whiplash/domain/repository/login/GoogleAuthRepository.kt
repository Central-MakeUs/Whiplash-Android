package com.whiplash.domain.repository.login

import com.whiplash.domain.entity.GoogleUserEntity

interface GoogleAuthRepository {
    suspend fun signInWithGoogleToken(idToken: String): Result<GoogleUserEntity>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): GoogleUserEntity?
    fun getGoogleSignInIntent(): Any
    suspend fun handleSignInResult(data: Any?): Result<String>
}
