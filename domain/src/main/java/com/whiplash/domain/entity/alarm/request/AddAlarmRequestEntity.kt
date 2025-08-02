package com.whiplash.domain.entity.alarm.request

data class AddAlarmRequestEntity(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val alarmPurpose: String,
    val time: String, // 08:30
    val repeatDays: List<String>, // 월, 수, 금
    val soundType: String, // DEFAULT
)