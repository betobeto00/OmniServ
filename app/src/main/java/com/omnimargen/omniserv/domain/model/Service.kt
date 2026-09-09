package com.omnimargen.omniserv.domain.model

import java.util.Date

data class Service(
    val id: Long = 0,
    val clienteId: Long,
    val clienteNombre: String = "",
    val serviceTypeId: Long? = null,
    val tipoServicio: String,
    val fechaServicio: Date,
    val monto: Double,
    val estado: ServiceStatus = ServiceStatus.PENDIENTE,
    val operarios: List<ServiceOperator> = emptyList(),
    val notas: String = "",
    val numeroFactura: String? = null,
    val fechaCreacion: Date = Date()
)

data class ServiceOperator(
    val id: Long = 0,
    val serviceId: Long = 0,
    val operatorId: Long,
    val operatorNombre: String = "",
    val montoPago: Double = 0.0,
    val pagado: Boolean = false
)

enum class ServiceStatus {
    PENDIENTE,
    EN_PROGRESO,
    REALIZADO,
    CANCELADO
}
