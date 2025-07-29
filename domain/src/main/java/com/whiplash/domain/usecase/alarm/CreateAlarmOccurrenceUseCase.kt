package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.entity.alarm.response.CreateAlarmOccurrenceEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow

class CreateAlarmOccurrenceUseCase(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(alarmId: Long): Flow<Result<CreateAlarmOccurrenceEntity>> =
        repository.createAlarmOccurrence(alarmId)
}