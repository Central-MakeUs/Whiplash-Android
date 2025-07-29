package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseGetPlaceDetail = BaseResponse<GetPlaceDetailResult>

@Serializable
data class GetPlaceDetailResult(
    val address: String,
    val name: String,
)