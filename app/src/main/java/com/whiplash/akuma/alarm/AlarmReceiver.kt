package com.whiplash.akuma.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.whiplash.presentation.alarm.AlarmActivity
import timber.log.Timber
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "WHIPLASH_ALARM_CHANNEL"
        private var soundPool: SoundPool? = null
        private var loadedSoundId: Int? = null
        private var playingStreamId: Int? = null
        var wakeLock: PowerManager.WakeLock? = null

        fun stopAlarmSound() {
            try {
                playingStreamId?.let { streamId ->
                    soundPool?.stop(streamId)
                }
                loadedSoundId?.let { soundId ->
                    soundPool?.unload(soundId)
                }
                playingStreamId = null
                loadedSoundId = null

                soundPool?.release()
                soundPool = null

                wakeLock?.let { if (it.isHeld) it.release() }
                wakeLock = null

                Timber.d("## [알람음 정지 완료]")
            } catch (e: Exception) {
                Timber.e("## [알람음 정지 실패] ${e.message}")
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("## [AlarmReceiver] onReceive 호출됨 - action: ${intent.action}")

        if (intent.action == "com.whiplash.akuma.ALARM_TRIGGER") {
            val alarmId = intent.getIntExtra("alarmId", -1)
            val alarmPurpose = intent.getStringExtra("alarmPurpose") ?: "알람"
            val address = intent.getStringExtra("address") ?: ""
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            val soundType = intent.getStringExtra("soundType") ?: "알람 소리1"
            val originalHour = intent.getIntExtra("originalHour", 0)
            val originalMinute = intent.getIntExtra("originalMinute", 0)
            val dayOfWeek = intent.getIntExtra("dayOfWeek", -1)

            Timber.d("## [알람 수신] ID: $alarmId, 목적: $alarmPurpose, 주소: $address, 위도: $latitude, 경도: $longitude")

            // 반복 알람이면 다음 주의 같은 요일로 다시 스케줄링
            scheduleNextWeekAlarm(context, intent)

            acquireWakeLock(context)
            playAlarmSound(context, soundType)
            startVibration(context)
            createNotificationChannel(context)
            showAlarmNotification(context, alarmId, alarmPurpose, address, latitude, longitude, originalHour, originalMinute)
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

    private fun acquireWakeLock(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "WhiplashAlarm::WakeLock"
        )
        wakeLock?.acquire(5 * 60 * 1000L)
    }

    private fun playAlarmSound(context: Context, soundType: String) {
        try {
            stopAlarmSound()

            val resId = resolveSoundResId(context, soundType)

            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(1)
                .build()

            soundPool?.setOnLoadCompleteListener { sp, soundId, status ->
                if (status == 0) {
                    val streamId = sp.play(soundId, 1.0f, 1.0f, 1, -1, 1.0f)
                    playingStreamId = streamId
                    Timber.d("## [SoundPool 재생 시작] soundId=$soundId, streamId=$streamId")
                } else {
                    Timber.e("## [SoundPool 로드 실패] status=$status")
                }
            }

            if (resId != null) {
                loadedSoundId = soundPool?.load(context, resId, 1)
                Timber.d("## [SoundPool 로드 요청] soundType=$soundType, resId=$resId")
            } else {
                // resId 없으면 기본 알람음 사용
                val defaultAlarmResId = com.whiplash.presentation.R.raw.sound1
                loadedSoundId = soundPool?.load(context, defaultAlarmResId, 1)
                Timber.d("## [SoundPool 로드 요청] soundType=$soundType, defaultResId=$defaultAlarmResId")
            }

        } catch (e: Exception) {
            Timber.e("## [알람음 재생 실패] ${e.message}")
        }
    }

    private fun resolveSoundResId(context: Context, soundType: String?): Int? {
        if (soundType.isNullOrBlank()) return null
        val normalized = soundType.trim()

        return when {
            normalized.equals("sound_1", ignoreCase = true) ||
                    normalized.equals(context.getString(com.whiplash.presentation.R.string.sound_1), ignoreCase = true) ||
                    normalized.equals("알람 소리1", ignoreCase = true) -> com.whiplash.presentation.R.raw.sound1

            normalized.equals("sound_2", ignoreCase = true) ||
                    normalized.equals(context.getString(com.whiplash.presentation.R.string.sound_2), ignoreCase = true) ||
                    normalized.equals("알람 소리2", ignoreCase = true) -> com.whiplash.presentation.R.raw.sound2

            normalized.equals("sound_3", ignoreCase = true) ||
                    normalized.equals(context.getString(com.whiplash.presentation.R.string.sound_3), ignoreCase = true) ||
                    normalized.equals("알람 소리3", ignoreCase = true) -> com.whiplash.presentation.R.raw.sound3

            normalized.equals("sound_4", ignoreCase = true) ||
                    normalized.equals(context.getString(com.whiplash.presentation.R.string.sound_4), ignoreCase = true) ||
                    normalized.equals("알람 소리4", ignoreCase = true) -> com.whiplash.presentation.R.raw.sound4

            else -> null
        }
    }

    private fun startVibration(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 500, 500, 500, 500)
            vibrator.vibrate(pattern, 0)

        } catch (e: Exception) {
            Timber.e("## [진동 시작 실패] ${e.message}")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "알람",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Whiplash 알람 알림"
                enableVibration(true)
                enableLights(true)
                setSound(null, null)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showAlarmNotification(
        context: Context,
        alarmId: Int,
        purpose: String,
        address: String,
        latitude: Double,
        longitude: Double,
        hour: Int,
        minute: Int
    ) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("alarmId", alarmId)
            putExtra("alarmPurpose", purpose)
            putExtra("address", address)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("hour", hour)
            putExtra("minute", minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(com.whiplash.presentation.R.string.alarm_title))
            .setContentText(context.getString(com.whiplash.presentation.R.string.alarm_sub_title))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "알람 끄기",
                createStopAlarmPendingIntent(context, alarmId)
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alarmId, notification)
    }

    private fun createStopAlarmPendingIntent(context: Context, alarmId: Int): PendingIntent {
        val intent = Intent("com.whiplash.akuma.STOP_ALARM").apply {
            putExtra("alarmId", alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}