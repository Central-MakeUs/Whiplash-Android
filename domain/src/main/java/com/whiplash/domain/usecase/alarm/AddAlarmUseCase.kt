package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.entity.alarm.request.AddAlarmRequest
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow

class AddAlarmUseCase(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(request: AddAlarmRequest): Flow<Result<Unit>> =
        repository.addAlarm(request)
}