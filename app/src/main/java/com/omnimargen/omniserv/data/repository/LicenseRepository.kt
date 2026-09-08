package com.omnimargen.omniserv.data.repository

import android.content.Context
import com.omnimargen.omniserv.data.local.dao.LicenseDao
import com.omnimargen.omniserv.data.mapper.toDomain
import com.omnimargen.omniserv.data.mapper.toEntity
import com.omnimargen.omniserv.domain.model.License
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseRepository @Inject constructor(
    private val licenseDao: LicenseDao,
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("omniserv_prefs", Context.MODE_PRIVATE)

    suspend fun getLicense(): License? = licenseDao.getLicense()?.toDomain()

    suspend fun saveLicense(license: License) = licenseDao.insert(license.toEntity())

    suspend fun deleteLicense() = licenseDao.deleteAll()

    fun getInstallTime(): Long {
        var installTime = prefs.getLong("install_time", 0L)
        if (installTime == 0L) {
            installTime = System.currentTimeMillis()
            prefs.edit().putLong("install_time", installTime).apply()
        }
        return installTime
    }
}
