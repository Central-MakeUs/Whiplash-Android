package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T,
)