package com.whiplash.domain.entity

data class GetAlarmEntity(
    val alarmId: Long,
    val alarmName: String,
    val type: String,
    val repeatDays: List<String>,
    val time: String,
    val placeName: String,
    val latitude: Double,
    val longitude: Double
)