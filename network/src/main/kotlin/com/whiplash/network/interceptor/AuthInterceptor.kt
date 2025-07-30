package com.whiplash.network.interceptor

import android.util.Log
import com.whiplash.domain.provider.TokenProvider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.encodedPath

        // 소셜로그인과 토큰 재발급 API는 특별 처리
        if (url.contains("auth/social-login")) {
            return chain.proceed(originalRequest)
        }

        // 토큰 재발급 api는 리프레시 토큰 사용
        if (url.contains("auth/reissue")) {
            val refreshToken = runBlocking { tokenProvider.refreshToken.firstOrNull() }
            val requestWithRefreshToken = originalRequest.newBuilder()
                .addHeader("Authorization", refreshToken ?: "")
                .build()
            return chain.proceed(requestWithRefreshToken)
        }

        // 그 외 API는 access token 추가
        val accessToken = runBlocking { tokenProvider.accessToken.firstOrNull() }
        val requestWithToken = originalRequest.newBuilder()
            .addHeader("Authorization", accessToken ?: "")
            .build()

        val response = chain.proceed(requestWithToken)

        // 토큰 만료 시 재발급 시도
        if (shouldRefreshToken(response, accessToken)) {
            return handleTokenRefresh(chain, originalRequest, response)
        }

        return response
    }

    private fun shouldRefreshToken(response: Response, accessToken: String?): Boolean {
        if (accessToken == null) return false
        if (response.code == 401) return true

        return try {
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            responseBody.contains("AUTH_102") || responseBody.contains("AUTH_103")
        } catch (e: Exception) {
            false
        }
    }

    private fun handleTokenRefresh(chain: Interceptor.Chain, originalRequest: Request, response: Response): Response {
        synchronized(this) {
            response.close()

            val deviceId = runBlocking { tokenProvider.deviceId.firstOrNull() } ?: return createUnauthorizedResponse()
            val refreshToken = runBlocking { tokenProvider.refreshToken.firstOrNull() } ?: return createUnauthorizedResponse()

            return try {
                val reissueRequest = Request.Builder()
                    .url("${originalRequest.url.scheme}://${originalRequest.url.host}:${originalRequest.url.port}/api/auth/reissue")
                    .post("""{"deviceId":"$deviceId"}""".toRequestBody("application/json".toMediaTypeOrNull()))
                    .addHeader("Authorization", refreshToken)
                    .build()
                Log.e("refresh", "## [토큰 재발급] 재발급 요청 : $reissueRequest")

                val reissueResponse = chain.proceed(reissueRequest)
                val responseBody = reissueResponse.body?.string()
                reissueResponse.close()

                if (reissueResponse.isSuccessful && responseBody?.contains("\"isSuccess\":true") == true) {
                    val newAccessToken = "\"accessToken\":\"([^\"]+)\"".toRegex().find(responseBody)?.groupValues?.get(1)
                    val newRefreshToken = "\"refreshToken\":\"([^\"]+)\"".toRegex().find(responseBody)?.groupValues?.get(1)

                    if (newAccessToken != null && newRefreshToken != null) {
                        Log.e("refresh", "## [토큰 재발급] 재발급 성공해서 토큰 저장. accessToken : $newAccessToken, refreshToken : $newRefreshToken")
                        runBlocking { tokenProvider.saveTokens(newAccessToken, newRefreshToken) }

                        return chain.proceed(originalRequest.newBuilder()
                            .header("Authorization", newAccessToken)
                            .build())
                    }
                }

                runBlocking { tokenProvider.clearTokens() }
                createUnauthorizedResponse()
            } catch (e: Exception) {
                runBlocking { tokenProvider.clearTokens() }
                createUnauthorizedResponse()
            }
        }
    }

    private fun createUnauthorizedResponse(): Response {
        return Response.Builder()
            .request(Request.Builder().url("http://localhost/").build())
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody(null))
            .build()
    }
}