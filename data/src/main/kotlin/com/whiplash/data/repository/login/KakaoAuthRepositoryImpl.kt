package com.whiplash.data.repository.login

import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.user.UserApiClient
import com.whiplash.domain.entity.KakaoUserEntity
import com.whiplash.domain.repository.login.KakaoAuthRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

class KakaoAuthRepositoryImpl @Inject constructor(
) : KakaoAuthRepository {

    override suspend fun getUserInfoWithToken(accessToken: String): Result<KakaoUserEntity> {
        return try {
            val user = getUserInfo()
            Timber.d("## [카카오 레포 impl] 사용자 정보 조회 성공 : $user")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e("## [카카오 레포 impl] 사용자 정보 조회 실패 : $e")
            Result.failure(e)
        }
    }

    override suspend fun signOutKakao(): Result<Unit> {
        return try {
            suspendCancellableCoroutine { continuation ->
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Timber.e("## [카카오 레포 impl] 카카오 로그아웃 실패 : $error")
                    } else {
                        Timber.d("## [카카오 레포 impl] 카카오 로그아웃 성공")
                        continuation.resume(Unit)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentKakaoUser(): Result<KakaoUserEntity?> {
        return try {
            // 먼저 토큰 존재 여부 확인
            val currentToken = TokenManagerProvider.instance.manager.getToken()
            if (currentToken == null) {
                Timber.d("## [카카오 레포 impl] 토큰이 없음 - 비로그인 상태")
                return Result.success(null)
            }

            // 토큰 유효성 검사
            val isTokenValid = checkTokenValidity()
            if (!isTokenValid) {
                Timber.d("## [카카오 레포 impl] 토큰이 유효하지 않음 - 비로그인 상태")
                return Result.success(null)
            }

            // 토큰이 유효하면 사용자 정보 가져오기
            val user = getUserInfo()
            Timber.d("## [카카오 레포 impl] 카카오 유저 정보 조회 성공 : $user")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e("## [카카오 레포 impl] 카카오 유저 정보 조회 실패 : $e")
            Result.success(null) // 비로그인 상태로 간주
        }
    }

    private suspend fun checkTokenValidity(): Boolean = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.accessTokenInfo { _, error ->
            if (error != null) {
                Timber.e("## [카카오 레포 impl] 토큰 유효성 검사 실패 : $error")
                continuation.resume(false)
            } else {
                Timber.d("## [카카오 레포 impl] 토큰 유효성 검사 성공")
                continuation.resume(true)
            }
        }
    }

    private suspend fun getUserInfo(): KakaoUserEntity = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Timber.e("## [카카오 레포 impl] 사용자 정보 요청 실패 : $error")
            } else if (user != null) {
                Timber.d("## [카카오 레포 impl] 사용자 정보 요청 성공: $user")

                val kakaoUserEntity = KakaoUserEntity(
                    id = user.id.toString(),
                    email = user.kakaoAccount?.email,
                    nickname = user.kakaoAccount?.profile?.nickname,
                    profileImageUrl = user.kakaoAccount?.profile?.profileImageUrl
                )
                continuation.resume(kakaoUserEntity)
            } else {
                Timber.d("## [카카오 레포 impl] 사용자 정보가 null")
            }
        }
    }
}