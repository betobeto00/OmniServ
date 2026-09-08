package com.omnimargen.omniserv.data.mapper

import com.omnimargen.omniserv.data.local.entity.ClientEntity
import com.omnimargen.omniserv.domain.model.Client
import java.util.Date

fun ClientEntity.toDomain(): Client = Client(
    id = id,
    nombre = nombre,
    telefono = telefono,
    direccion = direccion,
    fechaCreacion = Date(fechaCreacion)
)

fun Client.toEntity(): ClientEntity = ClientEntity(
    id = id,
    nombre = nombre,
    telefono = telefono,
    direccion = direccion,
    fechaCreacion = fechaCreacion.time
)
