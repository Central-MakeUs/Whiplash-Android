package com.whiplash.domain.usecase.login

import com.whiplash.domain.model.UserEntity
import com.whiplash.domain.repository.login.GoogleAuthRepository

class SignInWithGoogleUseCase(
    private val googleAuthRepository: GoogleAuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<UserEntity> =
        googleAuthRepository.signInWithGoogleToken(idToken)
}