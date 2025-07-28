package com.whiplash.network.authenticator

import com.whiplash.domain.provider.TokenProvider
import com.whiplash.network.api.AuthService
import com.whiplash.network.dto.request.RequestTokenReissue
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * 토큰 재발급 담당
 */
class TokenAuthenticator @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val authService: AuthService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 토큰 재발급 API는 재발급 로직에서 제외
        if (response.request.url.encodedPath.contains("auth/reissue")) {
            return null
        }

        val deviceId = runBlocking {
            tokenProvider.deviceId.firstOrNull()
        } ?: return null

        return try {
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

                response.request.newBuilder()
                    .header("Authorization", "Bearer ${tokenResult.accessToken}")
                    .build()
            } else {
                runBlocking { tokenProvider.clearTokens() }
                null
            }
        } catch (e: Exception) {
            runBlocking { tokenProvider.clearTokens() }
            null
        }
    }
}