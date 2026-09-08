package com.omnimargen.omniserv.license

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateDeviceFingerprint(): String {
        val deviceInfo = buildString {
            append(Build.BOARD)
            append(Build.BRAND)
            append(Build.DEVICE)
            append(Build.HARDWARE)
            append(Build.MANUFACTURER)
            append(Build.MODEL)
            append(Build.PRODUCT)
        }

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(deviceInfo.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
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
}
