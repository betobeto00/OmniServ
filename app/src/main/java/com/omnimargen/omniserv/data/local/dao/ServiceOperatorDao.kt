package com.omnimargen.omniserv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omnimargen.omniserv.data.local.entity.ServiceOperatorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceOperatorDao {
    @Query("SELECT * FROM service_operators WHERE serviceId = :serviceId")
    fun getByServiceId(serviceId: Long): Flow<List<ServiceOperatorEntity>>

    @Query("SELECT * FROM service_operators WHERE operatorId = :operatorId")
    fun getByOperatorId(operatorId: Long): Flow<List<ServiceOperatorEntity>>

    @Query("SELECT * FROM service_operators WHERE serviceId = :serviceId AND operatorId = :operatorId")
    suspend fun getByServiceAndOperator(serviceId: Long, operatorId: Long): ServiceOperatorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(serviceOperator: ServiceOperatorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(serviceOperators: List<ServiceOperatorEntity>)

    @Delete
    suspend fun delete(serviceOperator: ServiceOperatorEntity)

    @Query("DELETE FROM service_operators WHERE serviceId = :serviceId")
    suspend fun deleteByServiceId(serviceId: Long)

    @Query("DELETE FROM service_operators WHERE operatorId = :operatorId")
    suspend fun deleteByOperatorId(operatorId: Long)
}
