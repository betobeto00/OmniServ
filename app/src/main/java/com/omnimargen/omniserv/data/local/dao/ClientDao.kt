package com.omnimargen.omniserv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omnimargen.omniserv.data.local.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY nombre ASC")
    fun getAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE nombre LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getById(id: Long): ClientEntity?

    @Insert
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)
}
