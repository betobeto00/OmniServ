package com.omnimargen.omniserv.data.repository

import com.omnimargen.omniserv.data.local.dao.OperatorDao
import com.omnimargen.omniserv.data.mapper.toDomain
import com.omnimargen.omniserv.data.mapper.toEntity
import com.omnimargen.omniserv.domain.model.Operator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OperatorRepository @Inject constructor(
    private val operatorDao: OperatorDao
) {
    fun getActive(): Flow<List<Operator>> = operatorDao.getActive().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getAll(): Flow<List<Operator>> = operatorDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getById(id: Long): Operator? = operatorDao.getById(id)?.toDomain()

    suspend fun insert(operator: Operator): Long = operatorDao.insert(operator.toEntity())

    suspend fun update(operator: Operator) = operatorDao.update(operator.toEntity())

    suspend fun delete(operator: Operator) = operatorDao.delete(operator.toEntity())
}
