package com.whiplash.akuma

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.whiplash.domain.entity.auth.request.RegisterFcmTokenRequestEntity
import com.whiplash.domain.usecase.auth.RegisterFcmTokenUseCase
import com.whiplash.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WhiplashFirebaseMessagingService: FirebaseMessagingService() {

    @Inject
    lateinit var registerFcmTokenUseCase: RegisterFcmTokenUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val CHANNEL_ID = "whiplash_notification_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Timber.d("## [FCM] =================================")
        Timber.d("## [FCM] onMessageReceived 호출됨!")
        Timber.d("## [FCM] From: ${remoteMessage.from}")
        Timber.d("## [FCM] Message ID: ${remoteMessage.messageId}")
        Timber.d("## [FCM] Has notification: ${remoteMessage.notification != null}")
        Timber.d("## [FCM] Has data: ${remoteMessage.data.isNotEmpty()}")
        Timber.d("## [FCM] =================================")

        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("## [FCM] 데이터 메시지 처리: ${remoteMessage.data}")
            sendNotification(
                title = remoteMessage.data["title"],
                body = remoteMessage.data["body"],
                data = remoteMessage.data
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("## [FCM] onNewToken()에서 새로 받은 토큰 : $token")
        sendTokenToServer(token)
    }

    private fun sendNotification(
        title: String?, 
        body: String?, 
        data: Map<String, String> = emptyMap()
    ) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 알림 스타일 및 액션 설정
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.whiplash_app_icon)
            .setContentTitle(title ?: "눈 떠!")
            .setContentText(body ?: "새로운 알림이 있습니다")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body ?: "새로운 알림이 있습니다")
            )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = data["notification_id"]?.toIntOrNull() ?: NOTIFICATION_ID
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Timber.d("## [FCM] 알림 표시 완료 - ID: $notificationId")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "눈떠 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "눈떠 앱 알림 채널"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun sendTokenToServer(token: String) {
        serviceScope.launch {
            try {
                val request = RegisterFcmTokenRequestEntity(fcmToken = token)
                registerFcmTokenUseCase(request)
                    .catch { exception ->
                        Timber.e(exception, "## [FCM] FCM 토큰 서버 전송 실패")
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = {
                                Timber.d("## [FCM] FCM 토큰 서버 전송 성공")
                            },
                            onFailure = { exception ->
                                Timber.e(exception, "## [FCM] FCM 토큰 서버 전송 실패")
                            }
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "## [FCM] FCM 토큰 전송 중 예외 발생")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}