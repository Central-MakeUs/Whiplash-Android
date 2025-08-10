package com.whiplash.domain.repository.login

import com.whiplash.domain.entity.auth.request.LoginRequestEntity
import com.whiplash.domain.entity.auth.request.LogoutRequestEntity
import com.whiplash.domain.entity.auth.request.RegisterFcmTokenRequestEntity
import com.whiplash.domain.entity.auth.request.TokenReissueRequestEntity
import com.whiplash.domain.entity.auth.response.LoginResponseEntity
import com.whiplash.domain.entity.auth.response.TokenReissueResponseEntity
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun socialLogin(request: LoginRequestEntity): Flow<Result<LoginResponseEntity>>
    suspend fun reissueToken(request: TokenReissueRequestEntity): Flow<Result<TokenReissueResponseEntity>>
    suspend fun socialLogout(request: LogoutRequestEntity): Flow<Result<Unit>>
    suspend fun registerFcmToken(request: RegisterFcmTokenRequestEntity): Flow<Result<Unit>>
}