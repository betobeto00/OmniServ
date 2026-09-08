package com.omnimargen.omniserv.domain.model

import java.util.Date

data class Client(
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val direccion: String,
    val fechaCreacion: Date = Date()
)
