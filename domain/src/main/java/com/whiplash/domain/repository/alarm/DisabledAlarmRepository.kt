package com.whiplash.domain.repository.alarm

interface DisabledAlarmRepository {
    suspend fun setAlarmDisabled(alarmId: Long, dayOfWeek: Int, isDisabled: Boolean)
    suspend fun isAlarmDisabled(alarmId: Long, dayOfWeek: Int): Boolean
}