package com.omnimargen.omniserv.data.repository

import com.omnimargen.omniserv.data.local.dao.ClientDao
import com.omnimargen.omniserv.data.mapper.toDomain
import com.omnimargen.omniserv.data.mapper.toEntity
import com.omnimargen.omniserv.domain.model.Client
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao
) {
    fun getAll(): Flow<List<Client>> = clientDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    fun search(query: String): Flow<List<Client>> = clientDao.search(query).map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getById(id: Long): Client? = clientDao.getById(id)?.toDomain()

    suspend fun insert(client: Client): Long = clientDao.insert(client.toEntity())

    suspend fun update(client: Client) = clientDao.update(client.toEntity())

    suspend fun delete(client: Client) = clientDao.delete(client.toEntity())
}
