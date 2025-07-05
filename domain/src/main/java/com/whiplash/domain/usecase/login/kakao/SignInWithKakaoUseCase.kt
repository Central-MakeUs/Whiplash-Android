package com.whiplash.domain.usecase.login.kakao

import com.whiplash.domain.entity.KakaoLoginResult
import com.whiplash.domain.repository.login.KakaoAuthRepository

class SignInWithKakaoUseCase(
    private val kakaoAuthRepository: KakaoAuthRepository
) {
    suspend operator fun invoke(accessToken: String): Result<KakaoLoginResult> {
        return kakaoAuthRepository.getUserInfoWithToken(accessToken)
            .map { user ->
                KakaoLoginResult(
                    accessToken = accessToken,
                    user = user
                )
            }
    }
}