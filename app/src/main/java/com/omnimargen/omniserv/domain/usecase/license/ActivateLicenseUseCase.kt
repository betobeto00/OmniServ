package com.omnimargen.omniserv.domain.usecase.license

import com.omnimargen.omniserv.data.repository.LicenseRepository
import com.omnimargen.omniserv.domain.model.License
import com.omnimargen.omniserv.domain.model.LicenseStatus
import java.util.Date
import javax.inject.Inject

class ActivateLicenseUseCase @Inject constructor(
    private val licenseRepository: LicenseRepository
) {
    suspend operator fun invoke(
        licenseKey: String,
        empresaId: String,
        modulosActivos: List<String>,
        fechaExpiracion: Date
    ): LicenseStatus {
        val license = License(
            licenseKey = licenseKey,
            empresaId = empresaId,
            fechaEmision = Date(),
            fechaExpiracion = fechaExpiracion,
            modulosActivos = modulosActivos,
            firma = "" // Se validara contra TOG Platform en Fase 7
        )

        licenseRepository.saveLicense(license)

        return if (modulosActivos.contains("omniserv")) {
            LicenseStatus.Valid
        } else {
            LicenseStatus.InvalidSignature
        }
    }
}
