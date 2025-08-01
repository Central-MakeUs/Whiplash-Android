package com.whiplash.data.mapper

import com.whiplash.domain.entity.alarm.request.AddAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.DeleteAlarmRequestEntity
import com.whiplash.domain.entity.alarm.response.CreateAlarmOccurrenceEntity
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.network.dto.response.AlarmDto
import com.whiplash.network.dto.response.CreateAlarmOccurrenceResult
import com.whiplash.network.dto.request.RequestAddAlarms
import com.whiplash.network.dto.request.RequestDeleteAlarm
import javax.inject.Inject

class AlarmMapper @Inject constructor() {

    fun toEntity(alarmDto: AlarmDto): GetAlarmEntity {
        with(alarmDto) {
            return GetAlarmEntity(
                alarmId = alarmId,
                alarmPurpose = alarmPurpose,
                repeatsDays = repeatsDays,
                time = time,
                address = address,
                latitude = latitude,
                longitude = longitude,
                isToggleOn = isToggleOn
            )
        }
    }

    fun toNetworkRequest(addAlarmRequestEntity: AddAlarmRequestEntity): RequestAddAlarms {
        with(addAlarmRequestEntity) {
            return RequestAddAlarms(
                address = address,
                latitude = latitude,
                longitude = longitude,
                alarmPurpose = alarmPurpose,
                time = time,
                repeatDays = repeatDays,
                soundType = soundType
            )
        }
    }

    fun toCreateOccurrenceEntity(result: CreateAlarmOccurrenceResult): CreateAlarmOccurrenceEntity {
        return CreateAlarmOccurrenceEntity(
            occurrenceId = result.occurrenceId
        )
    }

    fun toNetworkRequest(deleteAlarmRequestEntity: DeleteAlarmRequestEntity): RequestDeleteAlarm {
        return RequestDeleteAlarm(
            reason = deleteAlarmRequestEntity.reason
        )
    }

}