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

    override suspend fun getAlarmList(): Flow<Result<List<GetAlarmEntity>>> =
        safeApiCallWithTransform(
            apiCall = { alarmService.getAlarms() },
            transform = { response ->
                response.result?.map { alarmMapper.toEntity(it) } ?: emptyList()
            }
        )

    override suspend fun addAlarm(request: AddAlarmRequest): Flow<Result<Unit>> =
        safeApiCallWithTransform(
            apiCall = { alarmService.addAlarm(alarmMapper.toNetworkRequest(request)) },
            transform = {}
        )

    override suspend fun createAlarmOccurrence(alarmId: Long): Flow<Result<CreateAlarmOccurrenceEntity>> =
        safeApiCallWithTransform(
            apiCall = { alarmService.createAlarmOccurrence(alarmId) },
            transform = { response ->
                response.result?.let { alarmMapper.toCreateOccurrenceEntity(it) }
                    ?: throw Exception("알람 발생 내역 생성 api 응답이 null")
            }
        )
}