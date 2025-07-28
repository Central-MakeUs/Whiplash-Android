package com.whiplash.domain.repository.login

import com.whiplash.domain.entity.auth.KakaoUserEntity

interface KakaoAuthRepository {
    suspend fun getUserInfoWithToken(accessToken: String): Result<KakaoUserEntity>
    suspend fun signOutKakao(): Result<Unit>
    suspend fun getCurrentKakaoUser(): Result<KakaoUserEntity?>
}