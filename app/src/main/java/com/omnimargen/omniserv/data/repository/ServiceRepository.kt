package com.omnimargen.omniserv.data.repository

import com.omnimargen.omniserv.data.local.dao.OperatorDao
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
    private val operatorDao: OperatorDao
) {
    fun getAll(): Flow<List<Service>> = serviceDao.getAll().map { entities ->
        entities.map { entity ->
            entity.toDomain().copy(operarios = getOperatorWithNames(entity.id))
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
        val entity = if (service.numeroFactura.isNullOrBlank()) {
            service.copy(numeroFactura = nextNumeroFactura()).toEntity()
        } else {
            service.toEntity()
        }
        return serviceDao.insert(entity)
    }

    private suspend fun nextNumeroFactura(): String {
        val siguiente = serviceDao.countWithFactura() + 1
        return "F-" + siguiente.toString().padStart(4, '0')
    }

    suspend fun update(service: Service) = serviceDao.update(service.toEntity())

    suspend fun delete(service: Service) = serviceDao.delete(service.toEntity())

    suspend fun assignOperators(serviceId: Long, operators: List<ServiceOperator>) {
        serviceOperatorDao.deleteByServiceId(serviceId)
        serviceOperatorDao.insertAll(operators.map { it.toEntity(serviceId) })
    }

    fun getOperatorsByServiceId(serviceId: Long): Flow<List<ServiceOperator>> =
        serviceOperatorDao.getByServiceId(serviceId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getOperatorWithNames(serviceId: Long): List<ServiceOperator> {
        val operators = serviceOperatorDao.getByServiceId(serviceId).first()
        return operators.map { entity ->
            val operator = operatorDao.getById(entity.operatorId)
            entity.toDomain().copy(
                operatorNombre = operator?.nombre ?: ""
            )
        }
    }
}
