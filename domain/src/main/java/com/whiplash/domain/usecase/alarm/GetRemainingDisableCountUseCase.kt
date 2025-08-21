package com.whiplash.domain.usecase.alarm

import com.whiplash.domain.entity.alarm.response.GetRemainingDisableCountEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.flow.Flow

class GetRemainingDisableCountUseCase(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(): Flow<Result<GetRemainingDisableCountEntity>> =
        repository.getRemainingDisableCount()
}