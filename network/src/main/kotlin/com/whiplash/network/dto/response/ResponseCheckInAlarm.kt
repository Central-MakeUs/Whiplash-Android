package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseCheckInAlarm = BaseResponse<CheckInAlarmResult>

@Serializable
data class CheckInAlarmResult(
    val latitude: Double,
    val longitude: Double
)