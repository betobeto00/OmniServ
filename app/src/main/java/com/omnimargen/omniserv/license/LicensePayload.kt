package com.omnimargen.omniserv.license

import java.util.Date

data class LicensePayload(
    val licenseKey: String,
    val empresaId: String,
    val fechaEmision: Date,
    val fechaExpiracion: Date,
    val modulosActivos: List<String>,
    val firma: String
)
