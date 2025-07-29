package com.whiplash.domain.usecase.auth

import com.whiplash.domain.entity.auth.request.LogoutRequestEntity
import com.whiplash.domain.repository.login.AuthRepository
import kotlinx.coroutines.flow.Flow

class SocialLogoutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: LogoutRequestEntity): Flow<Result<Unit>> =
        repository.socialLogout(request)
}