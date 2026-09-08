package com.omnimargen.omniserv.data.repository

import com.omnimargen.omniserv.data.local.dao.LicenseDao
import com.omnimargen.omniserv.data.mapper.toDomain
import com.omnimargen.omniserv.data.mapper.toEntity
import com.omnimargen.omniserv.domain.model.License
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseRepository @Inject constructor(
    private val licenseDao: LicenseDao
) {
    suspend fun getLicense(): License? = licenseDao.getLicense()?.toDomain()

    suspend fun saveLicense(license: License) = licenseDao.insert(license.toEntity())

    suspend fun deleteLicense() = licenseDao.deleteAll()
}
