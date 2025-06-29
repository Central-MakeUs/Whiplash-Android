package com.whiplash.domain.usecase.login

import com.whiplash.domain.model.UserEntity
import com.whiplash.domain.repository.login.GoogleAuthRepository

class GetCurrentUserUseCase(
    private val repository: GoogleAuthRepository
) {
    operator fun invoke(): UserEntity? = repository.getCurrentUser()
}