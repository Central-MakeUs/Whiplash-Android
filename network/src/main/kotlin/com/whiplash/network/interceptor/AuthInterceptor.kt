package com.whiplash.network.interceptor

import com.whiplash.domain.provider.TokenProvider
import com.whiplash.network.api.AuthService
import com.whiplash.network.dto.request.RequestTokenReissue
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * API 요청 시 헤더에 bearer token 추가 및 토큰 재발급 처리 담당
 */
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val authService: AuthService
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 토큰 재발급 api는 제외
        if (originalRequest.url.encodedPath.contains("auth/reissue")) {
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking {
            tokenProvider.accessToken.firstOrNull()
        }

        val requestWithToken = if (accessToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(requestWithToken)

        // 401 에러 시 토큰 재발급 시도
        if (response.code == 401 && accessToken != null) {
            response.close()

            val deviceId = runBlocking {
                tokenProvider.deviceId.firstOrNull()
            }

            if (deviceId != null) {
                try {
                    val reissueResponse = runBlocking {
                        authService.reissueToken(RequestTokenReissue(deviceId))
                    }

                    if (reissueResponse.isSuccessful && reissueResponse.body()?.isSuccess == true) {
                        val tokenResult = reissueResponse.body()!!.result

                        runBlocking {
                            tokenProvider.saveTokens(
                                tokenResult.accessToken,
                                tokenResult.refreshToken
                            )
                        }

                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${tokenResult.accessToken}")
                            .build()

                        return chain.proceed(newRequest)
                    } else {
                        runBlocking { tokenProvider.clearTokens() }
                    }
                } catch (e: Exception) {
                    runBlocking { tokenProvider.clearTokens() }
                }
            }
        }

        return response
    }
}