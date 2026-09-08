package com.omnimargen.omniserv.data.mapper

import com.omnimargen.omniserv.data.local.entity.OperatorEntity
import com.omnimargen.omniserv.domain.model.Operator
import java.util.Date

fun OperatorEntity.toDomain(): Operator = Operator(
    id = id,
    nombre = nombre,
    telefono = telefono,
    especialidad = especialidad,
    activo = activo,
    fechaCreacion = Date(fechaCreacion)
)

fun Operator.toEntity(): OperatorEntity = OperatorEntity(
    id = id,
    nombre = nombre,
    telefono = telefono,
    especialidad = especialidad,
    activo = activo,
    fechaCreacion = fechaCreacion.time
)
