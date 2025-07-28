package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseGetAlarmList(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<AlarmDto>,
)

@Serializable
data class AlarmDto(
    val alarmId: Long,
    val alarmName: String,
    val type: String,
    val repeatDays: List<String>,
    val time: String,
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
)