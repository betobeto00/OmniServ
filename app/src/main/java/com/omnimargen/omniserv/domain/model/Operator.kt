package com.omnimargen.omniserv.domain.model

import java.util.Date

data class Operator(
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val especialidad: String = "",
    val activo: Boolean = true,
    val fechaCreacion: Date = Date()
)
