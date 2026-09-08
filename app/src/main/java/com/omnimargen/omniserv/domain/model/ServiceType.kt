package com.omnimargen.omniserv.domain.model

import java.util.Date

data class ServiceType(
    val id: Long = 0,
    val nombre: String,
    val activo: Boolean = true,
    val fechaCreacion: Date = Date()
)
