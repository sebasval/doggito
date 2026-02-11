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

object NotificationHelper {

    const val EXTRA_DEEP_LINK = "deep_link"
    private var nextNotificationId = 2000

    fun showTaskReminder(context: Context, taskTitle: String) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REMINDERS,
            title = "Tarea pendiente",
            body = "No olvides: $taskTitle",
            icon = android.R.drawable.ic_menu_agenda
        )
    }

    fun showDailyTasksReady(context: Context) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REMINDERS,
            title = "Nuevas tareas disponibles",
            body = "¡Tus tareas del día están listas! Completa todas para mantener tu racha.",
            icon = android.R.drawable.ic_menu_today
        )
    }

    fun showCoinsEarned(context: Context, amount: Int, reason: String) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REWARDS,
            title = "+$amount DoggiCoins",
            body = reason,
            icon = android.R.drawable.ic_menu_my_calendar
        )
    }

    fun showRedeemExpiring(context: Context, code: String, daysLeft: Int) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REMINDERS,
            title = "Código por expirar",
            body = "Tu código $code expira en $daysLeft días. ¡Visita la tienda!",
            icon = android.R.drawable.ic_menu_recent_history
        )
    }

    fun showVetReminder(context: Context, vaccineName: String) {
        showNotification(
            context = context,
            channelId = DoggitoApp.CHANNEL_REMINDERS,
            title = "Recordatorio veterinario",
            body = "La vacuna $vaccineName está próxima. Agenda una cita.",
            icon = android.R.drawable.ic_menu_help
        )
    }

    /**
     * Muestra una notificacion push recibida de FCM con soporte para deep links.
     * @param deepLink Ruta de navegacion (ej: "shop/abc123", "home", "redeem/history")
     * @param channel Canal: "rewards", "reminders", o "location"
     */
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
            icon = android.R.drawable.ic_menu_send,
            deepLink = deepLink
        )
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        icon: Int,
        deepLink: String? = null
    ) {
        // Check permission on Android 13+
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
            nextNotificationId, // Unique request code per notification
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
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
