package com.omnimargen.omniserv.data.repository

import com.omnimargen.omniserv.data.local.dao.ServiceTypeDao
import com.omnimargen.omniserv.data.mapper.toDomain
import com.omnimargen.omniserv.data.mapper.toEntity
import com.omnimargen.omniserv.domain.model.ServiceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceTypeRepository @Inject constructor(
    private val serviceTypeDao: ServiceTypeDao
) {
    fun getActive(): Flow<List<ServiceType>> = serviceTypeDao.getActive().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getAll(): Flow<List<ServiceType>> = serviceTypeDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getById(id: Long): ServiceType? = serviceTypeDao.getById(id)?.toDomain()

    suspend fun insert(serviceType: ServiceType): Long =
        serviceTypeDao.insert(serviceType.toEntity())

    suspend fun update(serviceType: ServiceType) =
        serviceTypeDao.update(serviceType.toEntity())

    suspend fun delete(serviceType: ServiceType) =
        serviceTypeDao.delete(serviceType.toEntity())
}
