package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseAddAlarm = BaseResponse<AddAlarmResult>

@Serializable
data class AddAlarmResult(
    val alarmId: Long,
)
