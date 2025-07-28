package com.whiplash.network.api

import com.whiplash.network.dto.response.ResponseGetAlarmList
import retrofit2.http.GET

interface AlarmService {

    /**
     * 알람 목록 조회
     */
    @GET("alarms")
    suspend fun getAlarms(): ResponseGetAlarmList

}