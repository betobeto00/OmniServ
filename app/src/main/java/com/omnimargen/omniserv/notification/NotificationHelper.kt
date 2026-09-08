package com.omnimargen.omniserv.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.omnimargen.omniserv.MainActivity
import com.omnimargen.omniserv.OmniservApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun showReminderNotification(
        serviceId: Long,
        clientName: String,
        serviceType: String,
        amount: Double
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, serviceId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, OmniservApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Servicio proximo: $serviceType")
            .setContentText("$clientName - $$amount")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(serviceId.toInt(), notification)
    }

    fun cancelNotification(serviceId: Long) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(serviceId.toInt())
    }
}
