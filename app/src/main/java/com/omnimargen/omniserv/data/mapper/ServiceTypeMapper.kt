package com.omnimargen.omniserv.data.mapper

import com.omnimargen.omniserv.data.local.entity.ServiceTypeEntity
import com.omnimargen.omniserv.domain.model.ServiceType
import java.util.Date

fun ServiceTypeEntity.toDomain(): ServiceType = ServiceType(
    id = id,
    nombre = nombre,
    activo = activo,
    fechaCreacion = Date(fechaCreacion)
)

fun ServiceType.toEntity(): ServiceTypeEntity = ServiceTypeEntity(
    id = id,
    nombre = nombre,
    activo = activo,
    fechaCreacion = fechaCreacion.time
)
