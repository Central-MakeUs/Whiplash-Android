package com.whiplash.domain.repository.alarm

import com.whiplash.domain.entity.alarm.request.AddAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.DeleteAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.TurnOffAlarmRequestEntity
import com.whiplash.domain.entity.alarm.response.CheckInAlarmEntity
import com.whiplash.domain.entity.alarm.response.CreateAlarmOccurrenceEntity
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.domain.entity.alarm.response.TurnOffAlarmResponseEntity
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    suspend fun getAlarmList(): Flow<Result<List<GetAlarmEntity>>>
    suspend fun addAlarm(request: AddAlarmRequestEntity): Flow<Result<Unit>>
    suspend fun createAlarmOccurrence(alarmId: Long): Flow<Result<CreateAlarmOccurrenceEntity>>
    suspend fun deleteAlarm(alarmId: Long, deleteAlarmRequestEntity: DeleteAlarmRequestEntity): Flow<Result<Unit>>
    suspend fun turnOffAlarm(alarmId: Long, turnOffAlarmRequestEntity: TurnOffAlarmRequestEntity): Flow<Result<TurnOffAlarmResponseEntity>>
    suspend fun checkInAlarm(alarmId: Long): Flow<Result<CheckInAlarmEntity>>
}