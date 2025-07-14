package com.whiplash.data.mapper

import com.whiplash.domain.entity.GetAlarmEntity
import com.whiplash.network.dto.AlarmDto

fun AlarmDto.toEntity(): GetAlarmEntity = GetAlarmEntity(
    alarmId = alarmId,
    alarmName = alarmName,
    type = type,
    repeatDays = repeatDays,
    time = time,
    placeName = placeName,
    latitude = latitude,
    longitude = longitude
)