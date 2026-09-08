package com.omnimargen.omniserv.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omnimargen.omniserv.data.repository.LicenseRepository
import com.omnimargen.omniserv.domain.model.LicenseStatus
import com.omnimargen.omniserv.domain.usecase.license.ValidateLicenseUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class LicenseExpirationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val validateLicenseUseCase: ValidateLicenseUseCase,
    private val licenseRepository: LicenseRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val license = licenseRepository.getLicense()
            if (license == null) {
                return Result.success()
            }

            val now = System.currentTimeMillis()
            val expiry = license.fechaExpiracion.time
            val graceEnd = expiry + ValidateLicenseUseCase.GRACE_MILLIS

            val daysToExpiry = ((expiry - now) / (24 * 60 * 60 * 1000)).toInt()
            val daysToGraceEnd = ((graceEnd - now) / (24 * 60 * 60 * 1000)).toInt()

            // Notificar 3 días antes del vencimiento
            if (daysToExpiry == 3) {
                notificationHelper.showExpiryWarningNotification(
                    daysRemaining = 3,
                    isGracePeriod = false
                )
            }

            // Notificar día del vencimiento
            if (daysToExpiry == 0 && now < expiry) {
                notificationHelper.showExpiryWarningNotification(
                    daysRemaining = 0,
                    isGracePeriod = false
                )
            }

            // Notificar durante período de gracia (día 1, 3, 5, 7)
            if (now >= expiry && now < graceEnd) {
                if (daysToGraceEnd in setOf(7, 5, 3, 1)) {
                    notificationHelper.showExpiryWarningNotification(
                        daysRemaining = daysToGraceEnd,
                        isGracePeriod = true
                    )
                }
            }

            // Notificar fin de período de gracia
            if (daysToGraceEnd == 0 && now >= graceEnd) {
                notificationHelper.showExpiryWarningNotification(
                    daysRemaining = 0,
                    isGracePeriod = true
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}