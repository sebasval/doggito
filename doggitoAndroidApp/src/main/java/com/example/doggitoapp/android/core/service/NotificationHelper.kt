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

    private fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        icon: Int
    ) {
        // Check permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(nextNotificationId++, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
