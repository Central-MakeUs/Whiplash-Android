package com.whiplash.network.dto.response

typealias ResponseGetRemainingDisableCount = BaseResponse<GetRemainingDisableCountResult>

data class GetRemainingDisableCountResult(
    val remainingOffCount: Int,
)
