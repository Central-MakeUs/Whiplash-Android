package com.whiplash.network.dto.response

import kotlinx.serialization.Serializable

typealias ResponseCreateAlarmOccurrence = BaseResponse<CreateAlarmOccurrenceResult>

@Serializable
data class CreateAlarmOccurrenceResult(
    val occurrenceId: Int,
)