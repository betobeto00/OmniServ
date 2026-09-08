package com.omnimargen.omniserv.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omnimargen.omniserv.data.local.dao.ClientDao
import com.omnimargen.omniserv.data.local.dao.ServiceDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val serviceDao: ServiceDao,
    private val clientDao: ClientDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            val twoDaysFromNow = now + (2 * 24 * 60 * 60 * 1000)

            val upcomingServices = serviceDao.getUpcoming(now, twoDaysFromNow)

            upcomingServices.forEach { service ->
                val client = clientDao.getById(service.clienteId)
                val clientName = client?.nombre ?: "Cliente #${service.clienteId}"

                notificationHelper.showReminderNotification(
                    serviceId = service.id,
                    clientName = clientName,
                    serviceType = service.tipoServicio,
                    amount = service.monto
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
