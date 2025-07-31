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

    private val TAG = this::class.simpleName

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
            Log.d(TAG, "## [토큰 재발급] 조건 만족, 재발급 시도")
            return handleTokenRefresh(chain, originalRequest, response)
        }

        return response
    }

    private fun shouldRefreshToken(response: Response, accessToken: String?): Boolean {
        Log.d(TAG, "## [토큰 체크] accessToken : $accessToken, responseCode : ${response.code}")
        
        // runBlocking 대신 동기적으로 확인
        val refreshToken = try {
            runBlocking { 
                tokenProvider.refreshToken.firstOrNull() 
            }
        } catch (e: Exception) {
            Log.e(TAG, "## [토큰 체크] refreshToken 읽기 실패 : ${e.message}")
            null
        }
        
        Log.d(TAG, "## [토큰 체크] refreshToken : $refreshToken")
        
        if (refreshToken == null) {
            Log.d(TAG, "## [토큰 체크] refreshToken이 null -> 재로그인 필요")
            return false
        }
        
        // accessToken이 null이거나 401 응답인 경우 재발급 필요
        if (accessToken == null || response.code == 401) {
            Log.d(TAG, "## [토큰 체크] accessToken null 또는 401, 재발급 필요")
            return true
        }

        return try {
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.d(TAG, "## [토큰 체크] 응답 본문 : $responseBody")
            val needsRefresh = responseBody.contains("AUTH_102") || responseBody.contains("AUTH_103")
            Log.d(TAG, "## [토큰 체크] AUTH_102/103 포함 여부 : $needsRefresh")
            needsRefresh
        } catch (e: Exception) {
            Log.e(TAG, "## [토큰 체크] 응답 본문 읽기 실패 : ${e.message}")
            false
        }
    }

    private fun handleTokenRefresh(chain: Interceptor.Chain, originalRequest: Request, response: Response): Response {
        Log.d(TAG, "## [토큰 재발급 메서드] handleTokenRefresh 호출됨")
        synchronized(this) {
            response.close()

            val deviceId = try {
                runBlocking { tokenProvider.deviceId.firstOrNull() }
            } catch (e: Exception) {
                Log.e(TAG, "## [토큰 재발급] deviceId 읽기 실패 : ${e.message}")
                null
            }
            
            val refreshToken = try {
                runBlocking { tokenProvider.refreshToken.firstOrNull() }
            } catch (e: Exception) {
                Log.e(TAG, "## [토큰 재발급] refreshToken 읽기 실패 : ${e.message}")
                null
            }
            
            Log.d(TAG, "## [토큰 재발급] deviceId : $deviceId")
            Log.d(TAG, "## [토큰 재발급] refreshToken : $refreshToken")
            
            if (deviceId == null) {
                Log.e(TAG, "## [토큰 재발급] deviceId가 null -> 401 리턴")
                return createUnauthorizedResponse(originalRequest)
            }
            
            if (refreshToken == null) {
                Log.e(TAG, "## [토큰 재발급] refreshToken이 null -> 401 리턴")
                return createUnauthorizedResponse(originalRequest)
            }

            return try {
                val reissueRequest = Request.Builder()
                    .url("${originalRequest.url.scheme}://${originalRequest.url.host}:${originalRequest.url.port}/api/auth/reissue")
                    .post("""{"deviceId":"$deviceId"}""".toRequestBody("application/json".toMediaTypeOrNull()))
                    .addHeader("Authorization", refreshToken)
                    .build()
                Log.d(TAG, "## [토큰 재발급] 재발급 요청 생성 : ${reissueRequest.url}")

                val reissueResponse = chain.proceed(reissueRequest)
                val responseBody = reissueResponse.body?.string()
                Log.d(TAG, "## [토큰 재발급] 응답 : ${reissueResponse.code}, 본문 : $responseBody")
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
                createUnauthorizedResponse(originalRequest)
            } catch (e: Exception) {
                runBlocking { tokenProvider.clearTokens() }
                createUnauthorizedResponse(originalRequest)
            }
        }
    }

    private fun createUnauthorizedResponse(originalRequest: Request): Response {
        return Response.Builder()
            .request(originalRequest)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody(null))
            .build()
    }
}