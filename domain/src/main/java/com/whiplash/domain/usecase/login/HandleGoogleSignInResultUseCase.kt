package com.whiplash.domain.usecase.login

import com.whiplash.domain.repository.login.GoogleAuthRepository

class HandleGoogleSignInResultUseCase(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke(data: Any?): Result<String> =
        repository.handleSignInResult(data)
}