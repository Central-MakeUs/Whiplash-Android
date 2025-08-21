package com.whiplash.data.repository.alarm

import com.whiplash.domain.repository.alarm.AlarmRepository
import com.whiplash.network.api.AlarmService
import com.whiplash.data.mapper.AlarmMapper
import com.whiplash.data.repository.safeApiCallWithTransform
import com.whiplash.domain.entity.alarm.request.AddAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.CheckInAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.DeleteAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.TurnOffAlarmRequestEntity
import com.whiplash.domain.entity.alarm.response.AddAlarmEntity
import com.whiplash.domain.entity.alarm.response.CheckInAlarmEntity
import com.whiplash.domain.entity.alarm.response.CreateAlarmOccurrenceEntity
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.domain.entity.alarm.response.GetRemainingDisableCountEntity
import com.whiplash.domain.entity.alarm.response.TurnOffAlarmResponseEntity
import kotlinx.coroutines.flow.Flow
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

    override suspend fun getRemainingDisableCount(): Flow<Result<GetRemainingDisableCountEntity>> =
        safeApiCallWithTransform(
            apiCall = { alarmService.getRemainingDisableCount() },
            transform = { response ->
                response.result?.let { alarmMapper.toGetRemainingDisableCountEntity(it) }
                    ?: throw Exception("남은 알람 끄기 횟수 조회 api 응답이 null")
            }
        )

    override suspend fun addAlarm(request: AddAlarmRequestEntity): Flow<Result<AddAlarmEntity>> =
        safeApiCallWithTransform(
            apiCall = { alarmService.addAlarm(alarmMapper.toNetworkRequest(request)) },
            transform = { response ->
                response.result?.let { alarmMapper.toAddAlarmEntity(it) }
                    ?: throw Exception("알람 등록 api 응답이 null")
            }
        )

    override suspend fun createAlarmOccurrence(alarmId: Long): Flow<Result<CreateAlarmOccurrenceEntity>> =
        safeApiCallWithTransform(
            apiCall = { alarmService.createAlarmOccurrence(alarmId) },
            transform = { response ->
                response.result?.let { alarmMapper.toCreateOccurrenceEntity(it) }
                    ?: throw Exception("알람 발생 내역 생성 api 응답이 null")
            }
        )

    override suspend fun deleteAlarm(
        alarmId: Long,
        deleteAlarmRequestEntity: DeleteAlarmRequestEntity
    ): Flow<Result<Unit>> =
        safeApiCallWithTransform(
            apiCall = {
                alarmService.deleteAlarm(
                    alarmId,
                    alarmMapper.toNetworkRequest(deleteAlarmRequestEntity)
                )
            },
            transform = {}
        )

    override suspend fun turnOffAlarm(
        alarmId: Long,
        turnOffAlarmRequestEntity: TurnOffAlarmRequestEntity
    ): Flow<Result<TurnOffAlarmResponseEntity>> =
        safeApiCallWithTransform(
            apiCall = { alarmService.turnOffAlarm(alarmId, alarmMapper.toNetworkRequest(turnOffAlarmRequestEntity)) },
            transform = { response ->
                response.result?.let { alarmMapper.toTurnOffAlarmEntity(it) }
                    ?: throw Exception("알람 끄기 api 응답이 null")
            }
        )

    override suspend fun checkInAlarm(
        alarmId: Long,
        checkInAlarmRequestEntity: CheckInAlarmRequestEntity
    ): Flow<Result<CheckInAlarmEntity>> =
        safeApiCallWithTransform(
            apiCall = { alarmService.checkInAlarm(alarmId, alarmMapper.toNetworkRequest(checkInAlarmRequestEntity)) },
            transform = { response ->
                response.result?.let { alarmMapper.toCheckInAlarmEntity(it) }
                    ?: throw Exception("알람 도착 인증 api 응답이 null")
            }
        )

}