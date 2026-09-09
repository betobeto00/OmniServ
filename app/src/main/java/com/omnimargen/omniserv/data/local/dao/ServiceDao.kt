package com.omnimargen.omniserv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omnimargen.omniserv.data.local.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY fechaServicio DESC")
    fun getAll(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE fechaServicio BETWEEN :start AND :end ORDER BY fechaServicio ASC")
    fun getByDateRange(start: Long, end: Long): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE clienteId = :clientId ORDER BY fechaServicio DESC")
    fun getByClientId(clientId: Long): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE serviceTypeId = :serviceTypeId ORDER BY fechaServicio DESC")
    fun getByServiceTypeId(serviceTypeId: Long): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE estado = :estado ORDER BY fechaServicio DESC")
    fun getByEstado(estado: String): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE fechaServicio BETWEEN :now AND :twoDaysFromNow AND estado = 'PENDIENTE'")
    suspend fun getUpcoming(now: Long, twoDaysFromNow: Long): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getById(id: Long): ServiceEntity?

    @Query("SELECT COUNT(*) FROM services WHERE numeroFactura IS NOT NULL")
    suspend fun countWithFactura(): Int

    @Insert
    suspend fun insert(service: ServiceEntity): Long

    @Update
    suspend fun update(service: ServiceEntity)

    @Delete
    suspend fun delete(service: ServiceEntity)
}
