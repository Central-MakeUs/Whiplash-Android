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
    ) {
        Timber.d("## [AlarmScheduler] 알람 스케줄링 시작 - ID : $alarmId, 시간 : $time, 반복 : $repeatDays")

        val timeParts = time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        if (repeatDays.isEmpty()) {
            scheduleOneTimeAlarm(alarmId, hour, minute, alarmPurpose, address, soundType)
        } else {
            scheduleRepeatingAlarms(alarmId, hour, minute, repeatDays, alarmPurpose, address, soundType)
        }
    }

    override fun cancelAlarm(alarmId: Int) {
        val pendingIntent = createPendingIntent(alarmId, "", "")
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleOneTimeAlarm(
        alarmId: Int,
        hour: Int,
        minute: Int,
        purpose: String,
        address: String,
        soundType: String,
    ) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }

        val pendingIntent = createPendingIntent(alarmId, purpose, address, soundType)
        Timber.d("## [AlarmScheduler] 단발성 알람 설정 - 시간 : ${calendar.time}, 현재시간 : ${Calendar.getInstance().time}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Timber.d("## [AlarmScheduler] 알람 설정 완료")
    }

    private fun scheduleRepeatingAlarms(
        baseAlarmId: Int,
        hour: Int,
        minute: Int,
        repeatDays: List<String>,
        purpose: String,
        address: String,
        soundType: String,
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
            val alarmId = baseAlarmId * 10 + dayOfWeek

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.WEEK_OF_YEAR, 1)
            }

            val pendingIntent = createPendingIntent(alarmId, purpose, address, soundType)

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }

    private fun createPendingIntent(
        alarmId: Int,
        purpose: String,
        address: String,
        soundType: String = "알람 소리1"
    ): PendingIntent {
        val intent = Intent("com.whiplash.akuma.ALARM_TRIGGER").apply {
            component = ComponentName("com.whiplash.akuma", "com.whiplash.akuma.alarm.AlarmReceiver")
            putExtra("alarmId", alarmId)
            putExtra("alarmPurpose", purpose)
            putExtra("address", address)
            putExtra("soundType", soundType)
        }

        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}