package com.omnimargen.omniserv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omnimargen.omniserv.data.local.entity.LicenseEntity

@Dao
interface LicenseDao {
    @Query("SELECT * FROM license WHERE id = 1")
    suspend fun getLicense(): LicenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(license: LicenseEntity)

    @Query("DELETE FROM license")
    suspend fun deleteAll()
}
