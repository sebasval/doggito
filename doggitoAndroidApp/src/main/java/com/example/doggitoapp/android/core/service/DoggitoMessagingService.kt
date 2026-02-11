package com.example.doggitoapp.android.core.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DoggitoMessagingService : FirebaseMessagingService() {

    private val fcmTokenManager: FcmTokenManager by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "DoggitoFCM"
    }

    /**
     * Llamado cuando Firebase genera un nuevo token (primera vez o rotacion).
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo FCM token: ${token.take(20)}...")

        serviceScope.launch {
            fcmTokenManager.saveToken(token)
        }
    }

    /**
     * Llamado cuando llega una notificacion push (data message).
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        val title = message.data["title"] ?: message.notification?.title ?: "Doggito"
        val body = message.data["body"] ?: message.notification?.body ?: ""
        val deepLink = message.data["deep_link"]
        val channel = message.data["channel"] ?: "rewards"

        Log.d(TAG, "Titulo: $title, Body: $body, DeepLink: $deepLink")

        NotificationHelper.showPushNotification(
            context = this,
            title = title,
            body = body,
            deepLink = deepLink,
            channel = channel
        )
    }
}
