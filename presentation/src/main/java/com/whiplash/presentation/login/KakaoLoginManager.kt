package com.whiplash.presentation.login

import android.content.Context
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.whiplash.domain.entity.auth.response.KakaoUserEntity
import com.whiplash.domain.usecase.login.kakao.GetCurrentKakaoUserUseCase
import com.whiplash.domain.usecase.login.kakao.SignInWithKakaoUseCase
import com.whiplash.domain.usecase.login.kakao.SignOutKakaoUseCase
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@ActivityRetainedScoped
class KakaoLoginManager @Inject constructor(
    private val signInWithKakaoUseCase: SignInWithKakaoUseCase,
    private val signOutKakaoUseCase: SignOutKakaoUseCase,
    private val getCurrentKakaoUserUseCase: GetCurrentKakaoUserUseCase
) {

    /**
     * 현재 로그인된 카카오 사용자 정보 가져오기
     */
    suspend fun getCurrentUser(): Result<KakaoUserEntity?> = getCurrentKakaoUserUseCase()

    /**
     * 카카오 로그인 전체 프로세스 처리 (카카오 로그인 → 사용자 정보 조회)
     */
    suspend fun signIn(context: Context): Result<KakaoLoginResult> {
        return try {
            loginWithKakao(context)
                .fold(
                    onSuccess = { token ->
                        signInWithKakaoUseCase(token.accessToken)
                            .fold(
                                onSuccess = { loginResult ->
                                    Result.success(KakaoLoginResult(loginResult.user, token.accessToken))
                                },
                                onFailure = { e -> Result.failure(e) }
                            )
                    },
                    onFailure = { e -> Result.failure(e) }
                )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 카카오 로그아웃
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            signOutKakaoUseCase.invoke()
                .also { result ->
                    if (result.isSuccess) {
                        Timber.d("## [KakaoLoginManager] 카카오 로그아웃 성공")
                    } else {
                        Timber.e("## [KakaoLoginManager] 카카오 로그아웃 실패 : ${result.exceptionOrNull()?.message}")
                    }
                }
        } catch (e: Exception) {
            Timber.e("## [KakaoLoginManager] 카카오 로그아웃 에러 : ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 카카오 로그인 (토큰 받기)
     */
    private suspend fun loginWithKakao(context: Context): Result<OAuthToken> = suspendCancellableCoroutine { continuation ->
        // 기존 토큰 정리
        if (TokenManagerProvider.instance.manager.getToken() != null) {
            TokenManagerProvider.instance.manager.clear()
        }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                continuation.resume(Result.failure(error))
            } else if (token != null) {
                continuation.resume(Result.success(token))
            } else {
                continuation.resume(Result.failure(Exception("카카오 로그인 실패 - 토큰이 null")))
            }
        }

        if (LoginClient.instance.isKakaoTalkLoginAvailable(context)) {
            LoginClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        callback(null, error)
                        return@loginWithKakaoTalk
                    }
                    LoginClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else {
                    callback(token, null)
                }
            }
        } else {
            LoginClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }
}

data class KakaoLoginResult(
    val user: KakaoUserEntity,
    val accessToken: String
)