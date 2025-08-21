package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.repository.alarm.DisabledAlarmRepository

class GetAlarmDisabledUseCase(
    private val repository: DisabledAlarmRepository
) {
    suspend operator fun invoke(alarmId: Long, dayOfWeek: Int): Boolean =
        repository.isAlarmDisabled(alarmId, dayOfWeek)
}