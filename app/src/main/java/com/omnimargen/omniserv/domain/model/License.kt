package com.omnimargen.omniserv.domain.model

import java.util.Date

data class License(
    val id: Int = 1,
    val licenseKey: String,
    val empresaId: String,
    val fechaEmision: Date,
    val fechaExpiracion: Date,
    val modulosActivos: List<String>,
    val firma: String
)

sealed class LicenseStatus {
    object Valid : LicenseStatus()
    object Expired : LicenseStatus()
    object NotActivated : LicenseStatus()
    object InvalidSignature : LicenseStatus()
    data class GracePeriod(val daysRemaining: Int) : LicenseStatus()
}
