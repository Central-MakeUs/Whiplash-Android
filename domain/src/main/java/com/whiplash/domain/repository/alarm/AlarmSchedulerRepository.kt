package com.whiplash.domain.repository.alarm

interface AlarmSchedulerRepository {
    fun scheduleAlarm(
        alarmId: Int,
        time: String,
        repeatDays: List<String>,
        alarmPurpose: String,
        address: String,
        soundType: String,
    )

    fun cancelAlarm(alarmId: Int)
}