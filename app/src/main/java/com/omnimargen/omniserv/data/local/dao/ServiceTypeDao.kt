package com.omnimargen.omniserv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omnimargen.omniserv.data.local.entity.ServiceTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceTypeDao {
    @Query("SELECT * FROM service_types WHERE activo = 1 ORDER BY nombre ASC")
    fun getActive(): Flow<List<ServiceTypeEntity>>

    @Query("SELECT * FROM service_types ORDER BY nombre ASC")
    fun getAll(): Flow<List<ServiceTypeEntity>>

    @Query("SELECT * FROM service_types WHERE id = :id")
    suspend fun getById(id: Long): ServiceTypeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(serviceType: ServiceTypeEntity): Long

    @Update
    suspend fun update(serviceType: ServiceTypeEntity)

    @Delete
    suspend fun delete(serviceType: ServiceTypeEntity)
}
