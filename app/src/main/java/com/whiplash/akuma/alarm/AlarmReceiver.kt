package com.whiplash.akuma.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.SoundPool
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.whiplash.akuma.R
import com.whiplash.presentation.alarm.AlarmActivity
import timber.log.Timber

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "WHIPLASH_ALARM_CHANNEL"
        var mediaPlayer: MediaPlayer? = null
        private var soundPool: SoundPool? = null
        private var loadedSoundId: Int? = null
        private var playingStreamId: Int? = null
        var wakeLock: PowerManager.WakeLock? = null

        fun stopAlarmSound() {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                }
                mediaPlayer = null

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
            val soundType = intent.getStringExtra("soundType") ?: "알람 소리1"

            Timber.d("## [알람 수신] ID: $alarmId, 목적: $alarmPurpose, 주소: $address")

            acquireWakeLock(context)

            playAlarmSound(context, soundType)

            startVibration(context)

            createNotificationChannel(context)
            showAlarmNotification(context, alarmId, alarmPurpose, address)
        } else {
            Timber.d("## [AlarmReceiver] 다른 액션 수신: ${intent.action}")
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

            if (resId != null) {
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

                loadedSoundId = soundPool?.load(context, resId, 1)
                Timber.d("## [SoundPool 로드 요청] soundType=$soundType, resId=$resId")

            } else {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }

                Timber.d("## [MediaPlayer 재생 시작] soundType=$soundType, uri=$alarmUri")
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

    private fun showAlarmNotification(context: Context, alarmId: Int, purpose: String, address: String) {
        val intent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("alarmId", alarmId)
            putExtra("alarmPurpose", purpose)
            putExtra("address", address)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("알람")
            .setContentText("$purpose - $address")
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