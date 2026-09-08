package com.omnimargen.omniserv.license

import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("omniserv_license", Context.MODE_PRIVATE)

    /**
     * Hash SHA-256 que identifica ESTE teléfono ante tog-platform (licencia de un
     * solo dispositivo). Incluye ANDROID_ID (único por dispositivo para la firma
     * de la app) porque si solo se usaran campos Build.*, dos teléfonos del mismo
     * modelo producirían el mismo hash y podrían compartir la licencia.
     *
     * Se genera una vez y se cachea: el valor que reclamó la licencia debe seguir
     * siendo el mismo en cada verificación aunque algo cambie en el dispositivo.
     * Sobrevive a desinstalaciones (ANDROID_ID estable por firma de la app);
     * un factory reset lo cambia → ahí aplica transferencia vía soporte.
     */
    fun generateDeviceFingerprint(): String {
        prefs.getString(PREF_FINGERPRINT, null)?.let { return it }

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: ""

        val deviceInfo = buildString {
            append(androidId)
            append(':')
            append(Build.BOARD)
            append(Build.BRAND)
            append(Build.DEVICE)
            append(Build.HARDWARE)
            append(Build.MANUFACTURER)
            append(Build.MODEL)
            append(Build.PRODUCT)
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(deviceInfo.toByteArray()).joinToString("") { "%02x".format(it) }

        prefs.edit().putString(PREF_FINGERPRINT, hash).apply()
        return hash
    }

    fun generateActivationPayload(
        apiKey: String,
        deviceFingerprint: String
    ): Map<String, String> {
        return mapOf(
            "api_key" to apiKey,
            "device_fingerprint" to deviceFingerprint,
            "module" to "omniserv",
            "platform" to "android"
        )
    }

    private companion object {
        const val PREF_FINGERPRINT = "device_fingerprint"
    }
}
