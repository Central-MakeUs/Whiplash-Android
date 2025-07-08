package com.whiplash.domain.usecase.login.kakao

import com.whiplash.domain.repository.login.KakaoAuthRepository

class SignOutKakaoUseCase(
    private val repository: KakaoAuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.signOutKakao()
}