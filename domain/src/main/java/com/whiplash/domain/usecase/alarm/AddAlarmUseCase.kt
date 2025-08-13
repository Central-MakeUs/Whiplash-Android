package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.entity.alarm.request.AddAlarmRequestEntity
import com.whiplash.domain.entity.alarm.response.AddAlarmEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow

class AddAlarmUseCase(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(request: AddAlarmRequestEntity): Flow<Result<AddAlarmEntity>> =
        repository.addAlarm(request)
}