package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.repository.alarm.DisabledAlarmRepository

class SetAlarmDisabledUseCase(
    private val repository: DisabledAlarmRepository
) {
    suspend operator fun invoke(alarmId: Long, dayOfWeek: Int, isDisabled: Boolean) =
        repository.setAlarmDisabled(alarmId, dayOfWeek, isDisabled)
}