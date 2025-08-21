package com.whiplash.data.repository.alarm

import com.whiplash.data.datastore.DisabledAlarmDataStore
import com.whiplash.domain.repository.alarm.DisabledAlarmRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisabledAlarmRepositoryImpl @Inject constructor(
    private val disabledAlarmDataStore: DisabledAlarmDataStore
) : DisabledAlarmRepository {

    override suspend fun setAlarmDisabled(alarmId: Long, dayOfWeek: Int, isDisabled: Boolean) =
        disabledAlarmDataStore.setAlarmDisabled(alarmId, dayOfWeek, isDisabled)

    override suspend fun isAlarmDisabled(alarmId: Long, dayOfWeek: Int): Boolean =
        disabledAlarmDataStore.isAlarmDisabled(alarmId, dayOfWeek)
}