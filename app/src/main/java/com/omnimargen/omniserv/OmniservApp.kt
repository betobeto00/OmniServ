package com.omnimargen.omniserv

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.omnimargen.omniserv.notification.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class OmniservApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleDailyReminder()
        scheduleLicenseExpirationCheck()
    }

    private fun scheduleLicenseExpirationCheck() {
        notificationScheduler.scheduleLicenseExpirationCheck()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios de Servicio",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones de servicios proximos"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun scheduleDailyReminder() {
        notificationScheduler.scheduleDailyReminder()
    }

    companion object {
        const val CHANNEL_ID = "omniserv_recordatorios"
    }
}
