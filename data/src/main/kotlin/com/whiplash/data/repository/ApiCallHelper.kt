package com.whiplash.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

suspend fun <T, R> safeApiCallWithTransform(
    apiCall: suspend () -> Response<T>,
    transform: (T) -> R
): Flow<Result<R>> = flow {
    try {
        val response = apiCall()
        if (response.isSuccessful && response.body() != null) {
            val transformed = transform(response.body()!!)
            emit(Result.success(transformed))
        } else {
            emit(Result.failure(Exception("API 에러 : ${response.code()}")))
        }
    } catch (e: Exception) {
        emit(Result.failure(e))
    }
}