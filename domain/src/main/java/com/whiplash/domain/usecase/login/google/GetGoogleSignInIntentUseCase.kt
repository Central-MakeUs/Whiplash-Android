package com.whiplash.domain.usecase.login.google

import com.whiplash.domain.repository.login.GoogleAuthRepository

class GetGoogleSignInIntentUseCase(
    private val googleAuthRepository: GoogleAuthRepository
) {
    operator fun invoke(): Any = googleAuthRepository.getGoogleSignInIntent()
}