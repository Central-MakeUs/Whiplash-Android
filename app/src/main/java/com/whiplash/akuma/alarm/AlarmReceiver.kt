package com.whiplash.akuma.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.os.Build
import android.os.Bundle
import timber.log.Timber
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("## [AlarmReceiver] onReceive 호출됨 - action: ${intent.action}")

        if (intent.action == "com.whiplash.akuma.ALARM_TRIGGER") {
            val alarmId = intent.getIntExtra("alarmId", -1)
            val alarmPurpose = intent.getStringExtra("alarmPurpose") ?: "알람"
            val address = intent.getStringExtra("address") ?: ""
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            val soundType = intent.getStringExtra("soundType") ?: "알람 소리1"

            Timber.d("## [알람 수신] ID: $alarmId, 목적: $alarmPurpose, 주소: $address, 위도: $latitude, 경도: $longitude")

            // 포그라운드 서비스로 알람을 시작해서 앱을 강제종료해도 알람이 안 꺼지게 한다
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = "START_ALARM"
                putExtras(intent.extras ?: Bundle())
            }

            context.startForegroundService(serviceIntent)

            // 반복 알람 스케줄링
            scheduleNextWeekAlarm(context, intent)
        } else {
            Timber.d("## [AlarmReceiver] 다른 액션 수신: ${intent.action}")
        }
    }

    private fun scheduleNextWeekAlarm(context: Context, originalIntent: Intent) {
        val alarmId = originalIntent.getIntExtra("alarmId", -1)
        val alarmPurpose = originalIntent.getStringExtra("alarmPurpose") ?: "알람"
        val address = originalIntent.getStringExtra("address") ?: ""
        val latitude = originalIntent.getDoubleExtra("latitude", 0.0)
        val longitude = originalIntent.getDoubleExtra("longitude", 0.0)
        val soundType = originalIntent.getStringExtra("soundType") ?: "알람 소리1"
        val originalHour = originalIntent.getIntExtra("originalHour", 9)
        val originalMinute = originalIntent.getIntExtra("originalMinute", 0)
        val dayOfWeek = originalIntent.getIntExtra("dayOfWeek", -1)

        if (dayOfWeek in 1..7) {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.WEEK_OF_YEAR, 1)
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, originalHour)
                set(Calendar.MINUTE, originalMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val newIntent = Intent("com.whiplash.akuma.ALARM_TRIGGER").apply {
                component = ComponentName("com.whiplash.akuma", "com.whiplash.akuma.alarm.AlarmReceiver")
                putExtra("alarmId", alarmId)
                putExtra("alarmPurpose", alarmPurpose)
                putExtra("address", address)
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("soundType", soundType)
                putExtra("originalHour", originalHour)
                putExtra("originalMinute", originalMinute)
                putExtra("dayOfWeek", dayOfWeek)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId * 10 + dayOfWeek,
                newIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Timber.d("## [다음 주 알람 스케줄링] 시간: ${calendar.time}")
        }
    }
}