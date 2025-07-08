package com.whiplash.domain.usecase.login.google

import com.whiplash.domain.entity.GoogleUserEntity
import com.whiplash.domain.repository.login.GoogleAuthRepository

class GetCurrentUserUseCase(
    private val repository: GoogleAuthRepository
) {
    operator fun invoke(): GoogleUserEntity? = repository.getCurrentUser()
}