package com.omnimargen.omniserv.di

import android.content.Context
import androidx.room.Room
import com.omnimargen.omniserv.data.local.dao.ClientDao
import com.omnimargen.omniserv.data.local.dao.LicenseDao
import com.omnimargen.omniserv.data.local.dao.OperatorDao
import com.omnimargen.omniserv.data.local.dao.ServiceDao
import com.omnimargen.omniserv.data.local.dao.ServiceOperatorDao
import com.omnimargen.omniserv.data.local.dao.ServiceTypeDao
import com.omnimargen.omniserv.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "omniserv.db"
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }

    @Provides
    fun provideClientDao(db: AppDatabase): ClientDao = db.clientDao()

    @Provides
    fun provideServiceTypeDao(db: AppDatabase): ServiceTypeDao = db.serviceTypeDao()

    @Provides
    fun provideOperatorDao(db: AppDatabase): OperatorDao = db.operatorDao()

    @Provides
    fun provideServiceDao(db: AppDatabase): ServiceDao = db.serviceDao()

    @Provides
    fun provideServiceOperatorDao(db: AppDatabase): ServiceOperatorDao =
        db.serviceOperatorDao()

    @Provides
    fun provideLicenseDao(db: AppDatabase): LicenseDao = db.licenseDao()
}
