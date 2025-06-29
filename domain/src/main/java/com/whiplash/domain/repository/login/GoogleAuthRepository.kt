package com.whiplash.domain.repository.login

import com.whiplash.domain.model.UserEntity

interface GoogleAuthRepository {
    suspend fun signInWithGoogleToken(idToken: String): Result<UserEntity>
    suspend fun signOut(): Result<Unit>
    fun getCurrentUser(): UserEntity?
    fun getGoogleSignInIntent(): Any
    suspend fun handleSignInResult(data: Any?): Result<String>
}
