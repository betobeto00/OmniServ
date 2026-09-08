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
            .setContentTitle("Servicio próximo: $serviceType")
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

    fun showExpiryWarningNotification(daysRemaining: Int, isGracePeriod: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 9999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message) = when {
            isGracePeriod && daysRemaining > 0 -> 
                "Suscripción en período de gracia" to "Te quedan $daysRemaining días para renovar tu suscripción (3 USDT/mes) antes de perder el acceso."
            isGracePeriod && daysRemaining == 0 -> 
                "Suscripción expirada" to "El período de gracia terminó. Renueva tu suscripción (3 USDT/mes) para recuperar el acceso completo."
            !isGracePeriod && daysRemaining > 0 -> 
                "Suscripción por vencer" to "Tu suscripción vence en $daysRemaining días. Renueva (3 USDT/mes) para evitar interrupción del servicio."
            else -> 
                "Suscripción vencida hoy" to "Tu suscripción vence hoy. Renueva (3 USDT/mes) para mantener el acceso completo."
        }

        val notification = NotificationCompat.Builder(context, OmniservApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(9999, notification)
    }
}
