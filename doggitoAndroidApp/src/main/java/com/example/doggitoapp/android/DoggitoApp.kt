package com.example.doggitoapp.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.doggitoapp.android.data.sync.SyncManager
import com.example.doggitoapp.android.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class DoggitoApp : Application() {

    companion object {
        const val CHANNEL_LOCATION = "doggito_location"
        const val CHANNEL_REMINDERS = "doggito_reminders"
        const val CHANNEL_REWARDS = "doggito_rewards"
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@DoggitoApp)
            modules(appModules)
        }

        createNotificationChannels()
        SyncManager.schedulePeriodicSync(this)
        SyncManager.triggerImmediateSync(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val locationChannel = NotificationChannel(
                CHANNEL_LOCATION,
                "Seguimiento de actividad",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación durante el tracking de paseo/running"
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de tareas y citas veterinarias"
            }

            val rewardsChannel = NotificationChannel(
                CHANNEL_REWARDS,
                "Recompensas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de DoggiCoins y logros"
            }

            manager.createNotificationChannels(
                listOf(locationChannel, reminderChannel, rewardsChannel)
            )
        }
    }
}
