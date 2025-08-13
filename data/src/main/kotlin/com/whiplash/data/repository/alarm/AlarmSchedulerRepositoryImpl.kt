package com.whiplash.data.repository.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.whiplash.domain.repository.alarm.AlarmSchedulerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSchedulerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmSchedulerRepository {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleAlarm(
        alarmId: Int,
        time: String,
        repeatDays: List<String>,
        alarmPurpose: String,
        address: String,
        soundType: String,
        latitude: Double,
        longitude: Double,
    ) {
        Timber.d("## [AlarmScheduler] 알람 스케줄링 시작 - ID : $alarmId, 시간 : $time, 반복 : $repeatDays")

        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        if (repeatDays.isEmpty()) {
            scheduleOneTimeAlarm(alarmId, hour, minute, alarmPurpose, address, soundType, latitude, longitude)
        } else {
            scheduleRepeatingAlarms(alarmId, hour, minute, repeatDays, alarmPurpose, address, soundType, latitude, longitude)
        }
    }

    override fun cancelAlarm(alarmId: Int) {
        val pendingIntent = createPendingIntent(
            alarmId = alarmId,
            purpose = "",
            address = "",
            soundType = "",
            hour = 0,
            minute = 0,
            latitude = 0.0,
            longitude = 0.0
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleOneTimeAlarm(
        alarmId: Int,
        hour: Int,
        minute: Int,
        purpose: String,
        address: String,
        soundType: String,
        latitude: Double,
        longitude: Double,
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        val pendingIntent = createPendingIntent(alarmId, purpose, address, soundType, hour, minute, latitude, longitude)
        Timber.d("## [AlarmScheduler] 단발성 알람 설정 - 시간 : ${calendar.time}, 현재시간 : ${Calendar.getInstance().time}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Timber.d("## [AlarmScheduler] 알람 설정 완료")
    }

    private fun scheduleRepeatingAlarms(
        alarmId: Int,
        hour: Int,
        minute: Int,
        repeatDays: List<String>,
        purpose: String,
        address: String,
        soundType: String,
        latitude: Double,
        longitude: Double,
    ) {
        val dayMap = mapOf(
            "월" to Calendar.MONDAY,
            "화" to Calendar.TUESDAY,
            "수" to Calendar.WEDNESDAY,
            "목" to Calendar.THURSDAY,
            "금" to Calendar.FRIDAY,
            "토" to Calendar.SATURDAY,
            "일" to Calendar.SUNDAY
        )

        repeatDays.forEach { day ->
            val dayOfWeek = dayMap[day] ?: return@forEach
            // PendingIntent의 requestCode는 Int 사용
            val requestCode = alarmId * 10 + dayOfWeek

            val intent = Intent("com.whiplash.akuma.ALARM_TRIGGER").apply {
                component = ComponentName("com.whiplash.akuma", "com.whiplash.akuma.alarm.AlarmReceiver")
                putExtra("alarmId", alarmId)
                putExtra("dayOfWeek", dayOfWeek)
                putExtra("alarmPurpose", purpose)
                putExtra("address", address)
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("soundType", soundType)
                putExtra("originalHour", hour)
                putExtra("originalMinute", minute)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (before(Calendar.getInstance())) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Timber.d("## [AlarmScheduler] 반복 알람 설정 완료 - 요일 : $day, 시간 : ${calendar.time}")
        }
    }

    private fun createPendingIntent(
        alarmId: Int,
        purpose: String,
        address: String,
        soundType: String,
        hour: Int,
        minute: Int,
        latitude: Double,
        longitude: Double,
    ): PendingIntent {
        val intent = Intent("com.whiplash.akuma.ALARM_TRIGGER").apply {
            component = ComponentName("com.whiplash.akuma", "com.whiplash.akuma.alarm.AlarmReceiver")
            putExtra("alarmId", alarmId)
            putExtra("alarmPurpose", purpose)
            putExtra("address", address)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("soundType", soundType)
            putExtra("originalHour", hour)
            putExtra("originalMinute", minute)
        }

        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}