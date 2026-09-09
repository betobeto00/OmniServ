package com.omnimargen.omniserv.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        // v2: numeroFactura en services + pagado en service_operators
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE services ADD COLUMN numeroFactura TEXT")
                db.execSQL("ALTER TABLE service_operators ADD COLUMN pagado INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
    abstract fun clientDao(): ClientDao
    abstract fun serviceTypeDao(): ServiceTypeDao
    abstract fun operatorDao(): OperatorDao
    abstract fun serviceDao(): ServiceDao
    abstract fun serviceOperatorDao(): ServiceOperatorDao
    abstract fun licenseDao(): LicenseDao
}
