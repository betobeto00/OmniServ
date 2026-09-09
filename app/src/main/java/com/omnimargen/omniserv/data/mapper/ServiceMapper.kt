package com.omnimargen.omniserv.data.mapper

import com.omnimargen.omniserv.data.local.entity.LicenseEntity
import com.omnimargen.omniserv.data.local.entity.ServiceEntity
import com.omnimargen.omniserv.data.local.entity.ServiceOperatorEntity
import com.omnimargen.omniserv.domain.model.License
import com.omnimargen.omniserv.domain.model.Service
import com.omnimargen.omniserv.domain.model.ServiceOperator
import com.omnimargen.omniserv.domain.model.ServiceStatus
import org.json.JSONArray
import java.util.Date

fun ServiceEntity.toDomain(): Service = Service(
    id = id,
    clienteId = clienteId,
    clienteTelefono = clienteTelefono,
    serviceTypeId = serviceTypeId,
    tipoServicio = tipoServicio,
    fechaServicio = Date(fechaServicio),
    monto = monto,
    estado = ServiceStatus.valueOf(estado),
    notas = notas,
    numeroFactura = numeroFactura,
    fechaCreacion = Date(fechaCreacion)
)

fun Service.toEntity(): ServiceEntity = ServiceEntity(
    id = id,
    clienteId = clienteId,
    clienteTelefono = clienteTelefono,
    serviceTypeId = serviceTypeId,
    tipoServicio = tipoServicio,
    fechaServicio = fechaServicio.time,
    monto = monto,
    estado = estado.name,
    notas = notas,
    numeroFactura = numeroFactura,
    fechaCreacion = fechaCreacion.time
)

fun ServiceOperatorEntity.toDomain(): ServiceOperator = ServiceOperator(
    id = id,
    serviceId = serviceId,
    operatorId = operatorId,
    montoPago = montoPago,
    pagado = pagado
)

fun ServiceOperator.toEntity(serviceId: Long): ServiceOperatorEntity = ServiceOperatorEntity(
    id = id,
    serviceId = serviceId,
    operatorId = operatorId,
    montoPago = montoPago,
    pagado = pagado
)

fun LicenseEntity.toDomain(): License = License(
    id = id,
    licenseKey = licenseKey,
    empresaId = empresaId,
    fechaEmision = Date(fechaEmision),
    fechaExpiracion = Date(fechaExpiracion),
    modulosActivos = parseModulos(modulosActivos),
    firma = firma
)

fun License.toEntity(): LicenseEntity = LicenseEntity(
    id = id,
    licenseKey = licenseKey,
    empresaId = empresaId,
    fechaEmision = fechaEmision.time,
    fechaExpiracion = fechaExpiracion.time,
    modulosActivos = modulosActivosToString(modulosActivos),
    firma = firma
)

private fun parseModulos(json: String): List<String> {
    return try {
        val array = JSONArray(json)
        (0 until array.length()).map { array.getString(it) }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun modulosActivosToString(modulos: List<String>): String {
    val array = JSONArray()
    modulos.forEach { array.put(it) }
    return array.toString()
}
