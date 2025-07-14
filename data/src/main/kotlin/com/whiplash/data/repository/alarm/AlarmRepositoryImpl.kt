package com.whiplash.data.repository.alarm

import com.whiplash.data.mapper.toEntity
import com.whiplash.domain.entity.GetAlarmEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import com.whiplash.network.api.AlarmService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AlarmRepositoryImpl(
    private val service: AlarmService
): AlarmRepository {

    /**
     * 알람 목록 조회
     */
    override suspend fun getAlarmList(): Flow<Result<List<GetAlarmEntity>>> = flow {
        try {
            val response = service.getAlarms()
            if (response.isSuccess) {
                emit(Result.success(response.result.map { it.toEntity() }))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}