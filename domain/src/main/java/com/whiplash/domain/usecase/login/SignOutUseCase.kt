package com.whiplash.domain.usecase.login

import com.whiplash.domain.repository.login.GoogleAuthRepository

class SignOutUseCase(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.signOut()
}