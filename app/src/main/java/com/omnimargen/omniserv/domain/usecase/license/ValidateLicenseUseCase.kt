package com.omnimargen.omniserv.domain.usecase.license

import com.omnimargen.omniserv.data.repository.LicenseRepository
import com.omnimargen.omniserv.domain.model.LicenseStatus
import javax.inject.Inject

class ValidateLicenseUseCase @Inject constructor(
    private val licenseRepository: LicenseRepository
) {
    companion object {
        const val TRIAL_DAYS = 7
        const val TRIAL_MILLIS = TRIAL_DAYS * 24 * 60 * 60 * 1000L
        const val GRACE_DAYS = 7
        const val GRACE_MILLIS = GRACE_DAYS * 24 * 60 * 60 * 1000L
    }

    suspend operator fun invoke(): LicenseStatus {
        val license = licenseRepository.getLicense()

        if (license == null) {
            // Sin licencia: período de prueba de 7 días. La fecha de inicio se
            // persiste (inicio desde el botón "Disfrutar 7 días gratis" o, si no,
            // desde la instalación) para que el trial sobreviva reinicios de la app.
            val trialStart = licenseRepository.getTrialStartTime().takeIf { it > 0L }
                ?: licenseRepository.getInstallTime()
            val now = System.currentTimeMillis()
            val trialEnd = trialStart + TRIAL_MILLIS

            // Detectar manipulación de fecha: si el dispositivo tiene fecha anterior
            // a la de instalación, algo anda mal
            if (now < trialStart) {
                return LicenseStatus.TrialPeriod(TRIAL_DAYS)
            }

            return if (now < trialEnd) {
                val daysRemaining = ((trialEnd - now) / (24 * 60 * 60 * 1000)).toInt() + 1
                LicenseStatus.TrialPeriod(daysRemaining)
            } else {
                LicenseStatus.NotActivated
            }
        }

        val now = System.currentTimeMillis()
        val expiry = license.fechaExpiracion.time
        val graceEnd = expiry + GRACE_MILLIS

        return when {
            now < expiry -> LicenseStatus.Valid
            now < graceEnd -> {
                val daysRemaining = ((graceEnd - now) / (24 * 60 * 60 * 1000)).toInt() + 1
                LicenseStatus.GracePeriod(daysRemaining)
            }
            else -> LicenseStatus.Expired
        }
    }
}
