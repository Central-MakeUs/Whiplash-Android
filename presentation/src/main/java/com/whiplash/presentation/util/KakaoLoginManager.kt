package com.whiplash.presentation.util

import android.content.Context
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.TokenManagerProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

object KakaoLoginManager {

    suspend fun login(context: Context): Result<OAuthToken> = suspendCancellableCoroutine { continuation ->
        // 로그인 전에 기존 토큰 상태 확인해서 있으면 정리
        if (TokenManagerProvider.instance.manager.getToken() != null) {
            TokenManagerProvider.instance.manager.clear()
        }
        
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Timber.e("## [KakaoLoginManager] 카카오 로그인 실패 : $error")
                continuation.resume(Result.failure(error))
            } else if (token != null) {
                Timber.d("## [KakaoLoginManager] 카카오 로그인 성공. accessToken : ${token.accessToken}")
                continuation.resume(Result.success(token))
            } else {
                continuation.resume(Result.failure(Exception("## [KakaoLoginManager] 카카오 로그인 실패 - 토큰이 null")))
            }
        }

        if (LoginClient.instance.isKakaoTalkLoginAvailable(context)) {
            LoginClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    Timber.e("## [KakaoLoginManager] 카카오톡 로그인 실패 : $error")

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
