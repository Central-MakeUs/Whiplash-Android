package com.whiplash.network.interceptor

import com.whiplash.domain.provider.TokenProvider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * api 요청 시 헤더에 bearer token 추가 담당
 */
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken = runBlocking {
            tokenProvider.accessToken.firstOrNull()
        }

        val request = if (accessToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}