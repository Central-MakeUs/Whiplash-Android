package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseTurnOffAlarm = BaseResponse<TurnOffAlarmResult>

@Serializable
data class TurnOffAlarmResult(
    val offTargetDate: String,
    val offTargetDayOfWeek: String,
    val reactivateDate: String,
    val reactivateDayOfWeek: String,
    val remainingOffCount: Int,
)