package com.whiplash.akuma.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("## [StopAlarmReceiver] onReceive 호출됨 - action: ${intent.action}")

        if (intent.action == "com.whiplash.akuma.STOP_ALARM") {
            val alarmId = intent.getIntExtra("alarmId", -1)

            // AlarmService에 알람 정지하라는 신호 보냄
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = "STOP_ALARM"
                putExtra("alarmId", alarmId)
            }
            context.startService(serviceIntent)

            // 기존 알람의 알림 제거
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(alarmId)

            Timber.d("## [StopAlarmReceiver] 알람 정지 요청 완료. alarmId: $alarmId")
        }
    }
}