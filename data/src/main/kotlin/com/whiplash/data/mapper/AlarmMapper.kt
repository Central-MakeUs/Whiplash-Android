package com.whiplash.data.mapper

import android.view.PixelCopy.Request
import com.whiplash.domain.entity.alarm.request.AddAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.DeleteAlarmRequestEntity
import com.whiplash.domain.entity.alarm.request.TurnOffAlarmRequestEntity
import com.whiplash.domain.entity.alarm.response.AddAlarmEntity
import com.whiplash.domain.entity.alarm.response.CheckInAlarmEntity
import com.whiplash.domain.entity.alarm.response.CreateAlarmOccurrenceEntity
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.domain.entity.alarm.response.GetRemainingDisableCountEntity
import com.whiplash.domain.entity.alarm.response.TurnOffAlarmResponseEntity
import com.whiplash.network.dto.response.AlarmDto
import com.whiplash.network.dto.response.CreateAlarmOccurrenceResult
import com.whiplash.network.dto.request.RequestAddAlarms
import com.whiplash.network.dto.request.RequestDeleteAlarm
import com.whiplash.network.dto.request.RequestTurnOffAlarm
import com.whiplash.network.dto.response.AddAlarmResult
import com.whiplash.network.dto.response.CheckInAlarmResult
import com.whiplash.network.dto.response.GetRemainingDisableCountResult
import com.whiplash.network.dto.response.ResponseTurnOffAlarm
import com.whiplash.network.dto.response.TurnOffAlarmResult
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

    fun toAddAlarmEntity(result: AddAlarmResult): AddAlarmEntity {
        return AddAlarmEntity(
            alarmId = result.alarmId
        )
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

    fun toNetworkRequest(turnOffAlarmRequestEntity: TurnOffAlarmRequestEntity): RequestTurnOffAlarm {
        return RequestTurnOffAlarm(
            clientNow = turnOffAlarmRequestEntity.clientNow
        )
    }

    fun toTurnOffAlarmEntity(response: TurnOffAlarmResult): TurnOffAlarmResponseEntity {
        with(response) {
            return TurnOffAlarmResponseEntity(
                offTargetDate = offTargetDate,
                offTargetDayOfWeek = offTargetDayOfWeek,
                reactivateDate = reactivateDate,
                reactivateDayOfWeek = reactivateDayOfWeek,
                remainingOffCount = remainingOffCount
            )
        }
    }

    fun toCheckInAlarmEntity(result: CheckInAlarmResult): CheckInAlarmEntity {
        return CheckInAlarmEntity(
            latitude = result.latitude,
            longitude = result.longitude
        )
    }

    fun toGetRemainingDisableCountEntity(result: GetRemainingDisableCountResult): GetRemainingDisableCountEntity {
        return GetRemainingDisableCountEntity(
            remainingOffCount = result.remainingOffCount
        )
    }

}