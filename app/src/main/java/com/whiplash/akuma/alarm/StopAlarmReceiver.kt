package com.whiplash.akuma.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import timber.log.Timber

class StopAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("## [StopAlarmReceiver] onReceive 호출됨 - action: ${intent.action}")

        if (intent.action == "com.whiplash.akuma.STOP_ALARM") {
            val alarmId = intent.getIntExtra("alarmId", -1)
            AlarmReceiver.stopAlarmSound()
            stopVibration(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(alarmId)

            Timber.d("## [StopAlarmReceiver] 알람 정지 완료. alarmId: $alarmId")
        }
    }

    private fun stopVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            vibrator.cancel()
        } catch (e: Exception) {
            // 진동 정지 에러는 무시
        }
    }
}