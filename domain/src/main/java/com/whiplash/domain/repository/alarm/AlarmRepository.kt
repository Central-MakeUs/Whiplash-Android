package com.whiplash.domain.repository.alarm

import com.whiplash.domain.entity.alarm.request.AddAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.DeleteAlarmRequestEntity
import com.whiplash.domain.entity.alarm.response.CreateAlarmOccurrenceEntity
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    suspend fun getAlarmList(): Flow<Result<List<GetAlarmEntity>>>
    suspend fun addAlarm(request: AddAlarmRequestEntity): Flow<Result<Unit>>
    suspend fun createAlarmOccurrence(alarmId: Long): Flow<Result<CreateAlarmOccurrenceEntity>>
    suspend fun deleteAlarm(alarmId: Long, deleteAlarmRequestEntity: DeleteAlarmRequestEntity): Flow<Result<Unit>>
}