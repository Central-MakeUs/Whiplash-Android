package com.whiplash.domain.entity.alarm.response

data class GetAlarmEntity(
    val alarmId: Long,
    val alarmPurpose: String,
    val repeatsDays: List<String>,
    val time: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isToggleOn: Boolean,
)