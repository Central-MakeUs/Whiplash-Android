package com.whiplash.data.repository.alarm

import com.whiplash.domain.entity.GetAlarmEntity
import com.whiplash.domain.repository.alarm.AlarmRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
//    private val service: AlarmService // FIXME : api base url 추가되면 주석 해제
): AlarmRepository {

    /**
     * 알람 목록 조회
     */
    override suspend fun getAlarmList(): Flow<Result<List<GetAlarmEntity>>> = flow {
        try {
            delay(1000) // api 호출하는 것처럼 지연 추가
            val dummyAlarms = listOf(
                GetAlarmEntity(
                    alarmId = 1,
                    alarmName = "도서관 알람",
                    type = "RECURRING",
                    repeatDays = listOf("MONDAY", "FRIDAY"),
                    time = "09:00",
                    placeName = "경희대 도서관",
                    latitude = 37.123,
                    longitude = 127.456
                ),
                GetAlarmEntity(
                    alarmId = 2,
                    alarmName = "카페 알람",
                    type = "RECURRING",
                    repeatDays = listOf("MONDAY", "WEDNESDAY", "FRIDAY"),
                    time = "14:30",
                    placeName = "스타벅스 강남점",
                    latitude = 37.123,
                    longitude = 127.456
                ),
                GetAlarmEntity(
                    alarmId = 3,
                    alarmName = "운동 알람",
                    type = "RECURRING",
                    repeatDays = listOf("MONDAY", "WEDNESDAY", "FRIDAY"),
                    time = "18:00",
                    placeName = "헬스장",
                    latitude = 37.123,
                    longitude = 127.456
                ),
                GetAlarmEntity(
                    alarmId = 4,
                    alarmName = "도서관 알람2",
                    type = "RECURRING",
                    repeatDays = listOf("MONDAY", "FRIDAY"),
                    time = "09:10",
                    placeName = "경희대 도서관",
                    latitude = 37.123,
                    longitude = 127.456
                ),
                GetAlarmEntity(
                    alarmId = 5,
                    alarmName = "카페 알람2",
                    type = "RECURRING",
                    repeatDays = listOf("MONDAY", "WEDNESDAY", "FRIDAY"),
                    time = "14:40",
                    placeName = "스타벅스 강남점",
                    latitude = 37.123,
                    longitude = 127.456
                ),
                GetAlarmEntity(
                    alarmId = 6,
                    alarmName = "운동 알람2",
                    type = "RECURRING",
                    repeatDays = listOf("MONDAY", "WEDNESDAY", "FRIDAY"),
                    time = "18:10",
                    placeName = "헬스장",
                    latitude = 37.123,
                    longitude = 127.456
                )
            )

            emit(Result.success(dummyAlarms))

            // TODO : api 호출 가능해지면 아래 코드 사용
//            val response = service.getAlarms()
//            if (response.isSuccess) {
//                emit(Result.success(response.result.map { it.toEntity() }))
//            } else {
//                emit(Result.failure(Exception(response.message)))
//            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}