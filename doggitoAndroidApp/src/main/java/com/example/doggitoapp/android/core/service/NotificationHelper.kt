package com.example.doggitoapp.android.core.service

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.doggitoapp.android.DoggitoApp
import com.example.doggitoapp.android.MainActivity
import com.example.doggitoapp.android.R

object NotificationHelper {

    const val EXTRA_DEEP_LINK = "deep_link"
    private var nextNotificationId = 2000

    fun showTaskReminder(context: Context, taskTitle: String) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REMINDERS,
            title = "Tarea pendiente",
            body = "No olvides: $taskTitle"
        )
    }

    fun showDailyTasksReady(context: Context) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REMINDERS,
            title = "Nuevas tareas disponibles",
            body = "¡Tus tareas del día están listas! Completa todas para mantener tu racha."
        )
    }

    fun showCoinsEarned(context: Context, amount: Int, reason: String) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REWARDS,
            title = "+$amount DoggiCoins",
            body = reason
        )
    }

    fun showRedeemExpiring(context: Context, code: String, daysLeft: Int) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REMINDERS,
            title = "Código por expirar",
            body = "Tu código $code expira en $daysLeft días. ¡Visita la tienda!"
        )
    }

    fun showVetReminder(context: Context, vaccineName: String) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REMINDERS,
            title = "Recordatorio veterinario",
            body = "La vacuna $vaccineName está próxima. Agenda una cita."
        )
    }

    fun showPushNotification(
        context: Context,
        title: String,
        body: String,
        deepLink: String? = null,
        channel: String = "rewards"
    ) {
        val channelId = when (channel) {
            "reminders" -> DoggitoApp.CHANNEL_REMINDERS
            "location" -> DoggitoApp.CHANNEL_LOCATION
            else -> DoggitoApp.CHANNEL_REWARDS
        }

        showNotification(
            context = context,
            channelId = channelId,
            title = title,
            body = body,
            deepLink = deepLink
        )
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        deepLink: String? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (deepLink != null) {
                putExtra(EXTRA_DEEP_LINK, deepLink)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            nextNotificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (channelId == DoggitoApp.CHANNEL_REWARDS)
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(nextNotificationId++, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
