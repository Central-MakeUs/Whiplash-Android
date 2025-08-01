package com.whiplash.network.api

import com.whiplash.network.dto.request.RequestAddAlarms
import com.whiplash.network.dto.request.RequestDeleteAlarm
import com.whiplash.network.dto.request.RequestTurnOffAlarm
import com.whiplash.network.dto.response.BaseResponse
import com.whiplash.network.dto.response.ResponseCreateAlarmOccurrence
import com.whiplash.network.dto.response.ResponseDeleteAlarm
import com.whiplash.network.dto.response.ResponseGetAlarmList
import com.whiplash.network.dto.response.ResponseTurnOffAlarm
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AlarmService {

    /**
     * 알람 목록 조회
     */
    @GET("alarms")
    suspend fun getAlarms(): Response<ResponseGetAlarmList>

    /**
     * 알람 등록
     */
    @POST("alarms")
    suspend fun addAlarm(
        @Body requestAddAlarms: RequestAddAlarms
    ): Response<BaseResponse<Unit>>

    /**
     * 알람 발생 내역 생성
     *
     * 오늘 울려야 할 알람이 처음 울렸을 때 호출
     *
     * 알람 당 발생 내역은 하루에 1개만 생성 가능
     */
    @POST("alarms/{alarmId}/occurrences")
    suspend fun createAlarmOccurrence(
        @Path("alarmId") alarmId: Long
    ): Response<ResponseCreateAlarmOccurrence>

    /**
     * 알람 삭제
     */
    @DELETE("alarms/{alarmId}")
    suspend fun deleteAlarm(
        @Path("alarmId") alarmId: Long,
        @Body requestDeleteAlarm: RequestDeleteAlarm
    ): Response<BaseResponse<Unit>>

    /**
     * 알람 끄기
     */
    @POST("alarms/{alarmId}/off")
    suspend fun turnOffAlarm(
        @Path("alarmId") alarmId: Long,
        @Body requestTurnOffAlarm: RequestTurnOffAlarm
    ): Response<ResponseTurnOffAlarm>

}