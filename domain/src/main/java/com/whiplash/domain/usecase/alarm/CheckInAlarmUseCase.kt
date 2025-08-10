package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.entity.alarm.response.CheckInAlarmEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow

class CheckInAlarmUseCase(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarmId: Long): Flow<Result<CheckInAlarmEntity>> =
        repository.checkInAlarm(alarmId)
}