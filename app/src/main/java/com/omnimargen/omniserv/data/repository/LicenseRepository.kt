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

    /**
     * Inicio del período de prueba de 7 días: se fija la primera vez que el
     * usuario lo activa (botón "Disfrutar 7 días gratis") y se persiste para
     * que el trial sobreviva reinicios de la app.
     */
    fun startTrialPeriod() {
        if (prefs.getLong(PREF_TRIAL_START, 0L) == 0L) {
            prefs.edit().putLong(PREF_TRIAL_START, System.currentTimeMillis()).apply()
        }
    }

    /** 0 = el usuario aún no activó el trial (usa la fecha de instalación). */
    fun getTrialStartTime(): Long = prefs.getLong(PREF_TRIAL_START, 0L)

    fun saveAuthToken(token: String) {
        prefs.edit().putString(PREF_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? = prefs.getString(PREF_AUTH_TOKEN, null)

    fun clearAuthToken() {
        prefs.edit().remove(PREF_AUTH_TOKEN).apply()
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(PREF_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? = prefs.getString(PREF_USER_EMAIL, null)

    private companion object {
        const val PREF_TRIAL_START = "trial_start_time"
        const val PREF_AUTH_TOKEN = "auth_token"
        const val PREF_USER_EMAIL = "user_email"
    }
}
