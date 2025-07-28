package com.whiplash.data.repository.alarm

import com.whiplash.domain.repository.alarm.AlarmRepository
import com.whiplash.network.api.AlarmService
import com.whiplash.data.mapper.AlarmMapper
import com.whiplash.data.repository.safeApiCallWithTransform
import com.whiplash.domain.entity.alarm.request.AddAlarmRequest
import com.whiplash.domain.entity.alarm.response.CreateAlarmOccurrenceEntity
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmService: AlarmService,
    private val alarmMapper: AlarmMapper
) : AlarmRepository {

    override suspend fun getAlarmList(): Flow<Result<List<GetAlarmEntity>>> {
        return safeApiCallWithTransform(
            apiCall = { alarmService.getAlarms() },
            transform = { response -> response.result.map { alarmMapper.toEntity(it) } }
        )
    }

    override suspend fun addAlarm(request: AddAlarmRequest): Flow<Result<Unit>> {
        return safeApiCallWithTransform(
            apiCall = { alarmService.addAlarm(alarmMapper.toNetworkRequest(request)) },
            transform = {}
        )
    }

    override suspend fun createAlarmOccurrence(alarmId: Long): Flow<Result<CreateAlarmOccurrenceEntity>> {
        return safeApiCallWithTransform(
            apiCall = { alarmService.createAlarmOccurrence(alarmId) },
            transform = { response -> alarmMapper.toCreateOccurrenceEntity(response.result) }
        )
    }
}