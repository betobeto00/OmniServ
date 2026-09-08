package com.omnimargen.omniserv.domain.usecase.license

import com.omnimargen.omniserv.data.repository.LicenseRepository
import com.omnimargen.omniserv.domain.model.LicenseStatus
import javax.inject.Inject

class ValidateLicenseUseCase @Inject constructor(
    private val licenseRepository: LicenseRepository
) {
    suspend operator fun invoke(): LicenseStatus {
        val license = licenseRepository.getLicense()
            ?: return LicenseStatus.NotActivated

        val now = System.currentTimeMillis()
        val expiry = license.fechaExpiracion.time
        val graceEnd = expiry + (48 * 60 * 60 * 1000)

        return when {
            now < expiry -> LicenseStatus.Valid
            now < graceEnd -> {
                val daysRemaining = ((graceEnd - now) / (24 * 60 * 60 * 1000)).toInt()
                LicenseStatus.GracePeriod(daysRemaining)
            }
            else -> LicenseStatus.Expired
        }
    }
}
