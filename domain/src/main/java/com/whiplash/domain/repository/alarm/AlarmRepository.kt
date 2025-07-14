package com.whiplash.domain.repository.alarm

import com.whiplash.domain.entity.GetAlarmEntity
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    suspend fun getAlarmList(): Flow<Result<List<GetAlarmEntity>>>
}