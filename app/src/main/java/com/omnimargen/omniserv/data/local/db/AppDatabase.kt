package com.omnimargen.omniserv.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.omnimargen.omniserv.data.local.converter.DateConverter
import com.omnimargen.omniserv.data.local.dao.ClientDao
import com.omnimargen.omniserv.data.local.dao.LicenseDao
import com.omnimargen.omniserv.data.local.dao.OperatorDao
import com.omnimargen.omniserv.data.local.dao.ServiceDao
import com.omnimargen.omniserv.data.local.dao.ServiceOperatorDao
import com.omnimargen.omniserv.data.local.dao.ServiceTypeDao
import com.omnimargen.omniserv.data.local.entity.ClientEntity
import com.omnimargen.omniserv.data.local.entity.LicenseEntity
import com.omnimargen.omniserv.data.local.entity.OperatorEntity
import com.omnimargen.omniserv.data.local.entity.ServiceEntity
import com.omnimargen.omniserv.data.local.entity.ServiceOperatorEntity
import com.omnimargen.omniserv.data.local.entity.ServiceTypeEntity

@Database(
    entities = [
        ClientEntity::class,
        ServiceTypeEntity::class,
        OperatorEntity::class,
        ServiceEntity::class,
        ServiceOperatorEntity::class,
        LicenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun serviceTypeDao(): ServiceTypeDao
    abstract fun operatorDao(): OperatorDao
    abstract fun serviceDao(): ServiceDao
    abstract fun serviceOperatorDao(): ServiceOperatorDao
    abstract fun licenseDao(): LicenseDao
}
