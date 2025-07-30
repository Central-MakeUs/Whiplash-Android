package com.whiplash.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import org.json.JSONObject

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
            val errorMessage = try {
                val errorBody = response.errorBody()?.string()
                if (errorBody != null) {
                    val jsonObject = JSONObject(errorBody)
                    jsonObject.getString("message")
                } else {
                    response.message()
                }
            } catch (e: Exception) {
                response.message()
            }
            emit(Result.failure(Exception(errorMessage)))
        }
    } catch (e: Exception) {
        emit(Result.failure(e))
    }
}