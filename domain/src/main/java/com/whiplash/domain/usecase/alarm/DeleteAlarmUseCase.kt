package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.entity.alarm.request.DeleteAlarmRequestEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow

class DeleteAlarmUseCase(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(
        alarmId: Long,
        deleteAlarmRequestEntity: DeleteAlarmRequestEntity
    ): Flow<Result<Unit>> = repository.deleteAlarm(alarmId, deleteAlarmRequestEntity)
}