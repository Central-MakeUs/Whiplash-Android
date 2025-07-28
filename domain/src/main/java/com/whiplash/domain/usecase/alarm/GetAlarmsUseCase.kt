package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow

class GetAlarmsUseCase(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(): Flow<Result<List<GetAlarmEntity>>> = repository.getAlarmList()
}