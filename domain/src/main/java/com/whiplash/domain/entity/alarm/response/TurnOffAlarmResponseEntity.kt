package com.whiplash.domain.entity.alarm.response

data class TurnOffAlarmResponseEntity(
    val offTargetDate: String,
    val offTargetDayOfWeek: String,
    val reactivateDate: String,
    val reactivateDayOfWeek: String,
    val remainingOffCount: Int,
)
