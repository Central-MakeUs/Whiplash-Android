package com.whiplash.domain.repository.alarm

interface AlarmSchedulerRepository {
    fun scheduleAlarm(
        alarmId: Int,
        time: String,
        repeatDays: List<String>,
        alarmPurpose: String,
        address: String,
        soundType: String,
        latitude: Double,
        longitude: Double,
    )

    fun cancelAlarm(alarmId: Int)
}