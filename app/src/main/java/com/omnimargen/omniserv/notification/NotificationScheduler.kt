package com.omnimargen.omniserv.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleDailyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "omniserv_daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelReminder(serviceId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$serviceId")
    }
}
