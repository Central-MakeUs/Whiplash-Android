package com.whiplash.data.repository.login

import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.user.UserApiClient
import com.whiplash.domain.entity.auth.response.KakaoUserEntity
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
            // 먼저 토큰 존재 여부 확인
            val currentToken = TokenManagerProvider.instance.manager.getToken()
            if (currentToken == null) {
                Timber.d("## [카카오 레포 impl] 이미 로그아웃된 상태")
                return Result.success(Unit)
            }

            suspendCancellableCoroutine { continuation ->
                // logout 사용 시 재로그인하면 팝업이 표시되지 않음
                // unlink를 써야 로그아웃 후 재로그인 시 팝업 표시됨
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        Timber.e("## [카카오 레포 impl] 카카오 로그아웃 실패 : $error")
                        // 토큰이 이미 만료된 경우에도 로컬 토큰 삭제
                        TokenManagerProvider.instance.manager.clear()
                        continuation.resume(Unit)
                    } else {
                        Timber.d("## [카카오 레포 impl] 카카오 로그아웃 성공")
                        TokenManagerProvider.instance.manager.clear()
                        continuation.resume(Unit)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            TokenManagerProvider.instance.manager.clear()
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
                Timber.d("## [카카오 레포 impl] 사용자 정보 요청 성공 : $user")

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