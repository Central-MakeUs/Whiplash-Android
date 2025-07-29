package com.whiplash.domain.usecase.login.google

import com.whiplash.domain.entity.auth.response.GoogleUserEntity
import com.whiplash.domain.repository.login.GoogleAuthRepository

class SignInWithGoogleUseCase(
    private val googleAuthRepository: GoogleAuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<GoogleUserEntity> =
        googleAuthRepository.signInWithGoogleToken(idToken)
}