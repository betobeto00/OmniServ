package com.omnimargen.omniserv.data.repository

import com.omnimargen.omniserv.data.local.dao.ClientDao
import com.omnimargen.omniserv.data.local.dao.OperatorDao
import com.omnimargen.omniserv.data.local.dao.OperatorPaymentSummary
import com.omnimargen.omniserv.data.local.dao.ServiceDao
import com.omnimargen.omniserv.data.local.dao.ServiceOperatorDao
import com.omnimargen.omniserv.data.mapper.toDomain
import com.omnimargen.omniserv.data.mapper.toEntity
import com.omnimargen.omniserv.domain.model.Service
import com.omnimargen.omniserv.domain.model.ServiceOperator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(
    private val serviceDao: ServiceDao,
    private val serviceOperatorDao: ServiceOperatorDao,
    private val operatorDao: OperatorDao,
    private val clientDao: ClientDao
) {
    fun getAll(): Flow<List<Service>> = serviceDao.getAll().map { entities ->
        entities.map { entity ->
            val client = clientDao.getById(entity.clienteId)
            entity.toDomain().copy(
                clienteTelefono = client?.telefono ?: "",
                operarios = getOperatorWithNames(entity.id)
            )
        }
    }

    fun getByDateRange(start: Long, end: Long): Flow<List<Service>> =
        serviceDao.getByDateRange(start, end).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getByClientId(clientId: Long): Flow<List<Service>> =
        serviceDao.getByClientId(clientId).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getByServiceTypeId(serviceTypeId: Long): Flow<List<Service>> =
        serviceDao.getByServiceTypeId(serviceTypeId).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getByEstado(estado: String): Flow<List<Service>> =
        serviceDao.getByEstado(estado).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getUpcoming(now: Long, twoDaysFromNow: Long): List<Service> =
        serviceDao.getUpcoming(now, twoDaysFromNow).map { it.toDomain() }

    suspend fun getById(id: Long): Service? = serviceDao.getById(id)?.toDomain()

    suspend fun insert(service: Service): Long {
        val entity = service.toEntity()
        val id = serviceDao.insert(entity)
        if (service.operarios.isNotEmpty()) {
            assignOperators(id, service.operarios)
        }
        return id
    }

    suspend fun update(service: Service) {
        serviceDao.update(service.toEntity())
        assignOperators(service.id, service.operarios)
    }

    suspend fun delete(service: Service) = serviceDao.delete(service.toEntity())

    suspend fun assignOperators(serviceId: Long, operators: List<ServiceOperator>) {
        serviceOperatorDao.deleteByServiceId(serviceId)
        serviceOperatorDao.insertAll(operators.map { it.toEntity(serviceId) })
    }

    fun getOperatorsByServiceId(serviceId: Long): Flow<List<ServiceOperator>> =
        serviceOperatorDao.getByServiceId(serviceId).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getOperatorPaymentSummary(): Flow<List<OperatorPaymentSummary>> =
        serviceOperatorDao.getPaymentSummary()

    suspend fun getOperatorWithNames(serviceId: Long): List<ServiceOperator> {
        val operators = serviceOperatorDao.getByServiceId(serviceId).first()
        return operators.map { entity ->
            val operator = operatorDao.getById(entity.operatorId)
            entity.toDomain().copy(
                operatorNombre = operator?.nombre ?: ""
            )
        }
    }

    fun getServicesByOperatorId(operatorId: Long): Flow<List<Service>> =
        serviceOperatorDao.getServicesByOperatorId(operatorId).map { entities ->
            entities.map { entity ->
                val client = clientDao.getById(entity.clienteId)
                entity.toDomain().copy(
                    clienteTelefono = client?.telefono ?: "",
                    operarios = getOperatorWithNames(entity.id)
                )
            }
        }

    fun getOperatorServices(operatorId: Long): Flow<List<ServiceOperator>> =
        serviceOperatorDao.getOperatorServices(operatorId).map { entities ->
            entities.map { it.toDomain() }
        }
}
