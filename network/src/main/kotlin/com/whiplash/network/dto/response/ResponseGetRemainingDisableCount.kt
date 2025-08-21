package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseGetRemainingDisableCount = BaseResponse<GetRemainingDisableCountResult>

@Serializable
data class GetRemainingDisableCountResult(
    val remainingOffCount: Int,
)
