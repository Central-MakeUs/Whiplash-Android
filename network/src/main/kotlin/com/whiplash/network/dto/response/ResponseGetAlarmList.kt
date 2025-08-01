package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseGetAlarmList = BaseResponse<List<AlarmDto>>

@Serializable
data class AlarmDto(
    val alarmId: Long,
    val alarmPurpose: String,
    val repeatsDays: List<String>,
    val time: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isToggleOn: Boolean,
)