package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.entity.alarm.request.TurnOffAlarmRequestEntity
import com.whiplash.domain.entity.alarm.response.TurnOffAlarmResponseEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow

class TurnOffAlarmUseCase(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(
        alarmId: Long,
        turnOffAlarmRequestEntity: TurnOffAlarmRequestEntity
    ): Flow<Result<TurnOffAlarmResponseEntity>> =
        repository.turnOffAlarm(alarmId, turnOffAlarmRequestEntity)
}