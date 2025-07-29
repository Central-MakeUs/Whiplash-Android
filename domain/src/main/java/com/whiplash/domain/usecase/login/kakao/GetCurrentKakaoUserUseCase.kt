package com.whiplash.domain.usecase.login.kakao

import com.whiplash.domain.entity.auth.response.KakaoUserEntity
import com.whiplash.domain.repository.login.KakaoAuthRepository

class GetCurrentKakaoUserUseCase(
    private val repository: KakaoAuthRepository
) {
    suspend operator fun invoke(): Result<KakaoUserEntity?> = repository.getCurrentKakaoUser()
}