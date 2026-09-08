package com.omnimargen.omniserv.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.omnimargen.omniserv.data.local.entity.OperatorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperatorDao {
    @Query("SELECT * FROM operators WHERE activo = 1 ORDER BY nombre ASC")
    fun getActive(): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators ORDER BY nombre ASC")
    fun getAll(): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators WHERE id = :id")
    suspend fun getById(id: Long): OperatorEntity?

    @Insert
    suspend fun insert(operator: OperatorEntity): Long

    @Update
    suspend fun update(operator: OperatorEntity)

    @Delete
    suspend fun delete(operator: OperatorEntity)
}
