package com.whiplash.akuma.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.whiplash.presentation.alarm.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * 앱을 강제종료해도 알람이 계속 울리게 하기 위한 서비스
 */
@AndroidEntryPoint
class AlarmService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 9999
        private const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
        private var soundPool: SoundPool? = null
        private var loadedSoundId: Int? = null
        private var playingStreamId: Int? = null
        private var wakeLock: PowerManager.WakeLock? = null
        private var vibrator: Vibrator? = null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_ALARM" -> {
                val alarmId = intent.getIntExtra("alarmId", -1)
                val soundType = intent.getStringExtra("soundType") ?: "알람 소리1"
                startAlarm(alarmId, soundType, intent)
            }
            "STOP_ALARM" -> {
                stopAlarm()
                stopSelf()
            }
        }
        return START_STICKY // 시스템에 의해 종료되면 자동 재시작
    }

    private fun startAlarm(alarmId: Int, soundType: String, originalIntent: Intent) {
        acquireWakeLock()
        playAlarmSound(soundType)
        startVibration()

        val notification = createForegroundNotification(alarmId, originalIntent)
        startForeground(NOTIFICATION_ID, notification)

        Timber.d("## [AlarmService] 포그라운드 서비스로 알람 시작. alarmId: $alarmId")
    }

    private fun stopAlarm() {
        try {
            // 알람음 정지
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

            // 진동 정지
            vibrator?.cancel()
            vibrator = null

            // WakeLock 해제
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = null

            Timber.d("## [AlarmService] 알람 정지 완료")
        } catch (e: Exception) {
            Timber.e("## [AlarmService] 알람 정지 실패: ${e.message}")
        }
    }

    private fun createForegroundNotification(alarmId: Int, originalIntent: Intent): Notification {
        val alarmPurpose = originalIntent.getStringExtra("alarmPurpose") ?: "알람"
        val address = originalIntent.getStringExtra("address") ?: ""
        val latitude = originalIntent.getDoubleExtra("latitude", 0.0)
        val longitude = originalIntent.getDoubleExtra("longitude", 0.0)
        val hour = originalIntent.getIntExtra("originalHour", 0)
        val minute = originalIntent.getIntExtra("originalMinute", 0)

        val intent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("alarmId", alarmId)
            putExtra("alarmPurpose", alarmPurpose)
            putExtra("address", address)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            putExtra("hour", hour)
            putExtra("minute", minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, alarmId + 1000, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("알람이 울리고 있습니다")
            .setContentText("$alarmPurpose - $address")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(android.R.drawable.ic_media_pause, "알람 끄기", stopPendingIntent)
            .build()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "WhiplashAlarm::ServiceWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 1000L) // 10분
            Timber.d("## [AlarmService] WakeLock 획득")
        } catch (e: Exception) {
            Timber.e("## [AlarmService] WakeLock 획득 실패: ${e.message}")
        }
    }

    private fun playAlarmSound(soundType: String) {
        try {
            val resId = resolveSoundResId(soundType)

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
                    Timber.d("## [AlarmService] 알람음 재생 시작. soundId=$soundId, streamId=$streamId")
                } else {
                    Timber.e("## [AlarmService] 알람음 로드 실패. status=$status")
                }
            }

            if (resId != null) {
                loadedSoundId = soundPool?.load(this, resId, 1)
            } else {
                // 기본 알람음
                val defaultResId = com.whiplash.presentation.R.raw.sound1
                loadedSoundId = soundPool?.load(this, defaultResId, 1)
            }
        } catch (e: Exception) {
            Timber.e("## [AlarmService] 알람음 재생 실패: ${e.message}")
        }
    }

    private fun resolveSoundResId(soundType: String?): Int? {
        if (soundType.isNullOrBlank()) return null
        val normalized = soundType.trim()

        return when {
            normalized.equals("sound_1", ignoreCase = true) ||
                    normalized.equals(getString(com.whiplash.presentation.R.string.sound_1), ignoreCase = true) ||
                    normalized.equals("알람 소리1", ignoreCase = true) -> com.whiplash.presentation.R.raw.sound1

            normalized.equals("sound_2", ignoreCase = true) ||
                    normalized.equals(getString(com.whiplash.presentation.R.string.sound_2), ignoreCase = true) ||
                    normalized.equals("알람 소리2", ignoreCase = true) -> com.whiplash.presentation.R.raw.sound2

            normalized.equals("sound_3", ignoreCase = true) ||
                    normalized.equals(getString(com.whiplash.presentation.R.string.sound_3), ignoreCase = true) ||
                    normalized.equals("알람 소리3", ignoreCase = true) -> com.whiplash.presentation.R.raw.sound3

            normalized.equals("sound_4", ignoreCase = true) ||
                    normalized.equals(getString(com.whiplash.presentation.R.string.sound_4), ignoreCase = true) ||
                    normalized.equals("알람 소리4", ignoreCase = true) -> com.whiplash.presentation.R.raw.sound4

            else -> null
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 500, 500, 500, 500)
            vibrator?.vibrate(pattern, 0)
            Timber.d("## [AlarmService] 진동 시작")
        } catch (e: Exception) {
            Timber.e("## [AlarmService] 진동 시작 실패: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "알람 서비스",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Whiplash 알람 포그라운드 서비스"
            enableVibration(true)
            enableLights(true)
            setSound(null, null)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        Timber.d("## [AlarmService] 서비스 종료")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}