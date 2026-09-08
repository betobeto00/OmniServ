package com.omnimargen.omniserv.license

import android.util.Base64
import com.omnimargen.omniserv.domain.model.License
import com.omnimargen.omniserv.domain.model.LicenseStatus
import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LicenseValidator @Inject constructor() {

    companion object {
        private const val GRACE_PERIOD_HOURS = 48
        private const val GRACE_PERIOD_MS = GRACE_PERIOD_HOURS * 60 * 60 * 1000L
    }

    fun validateSignature(license: License, publicKeyPem: String): Boolean {
        return try {
            val publicKey = parsePublicKey(publicKeyPem)
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initVerify(publicKey)

            val dataToVerify = buildString {
                append(license.licenseKey)
                append(license.empresaId)
                append(license.fechaEmision.time)
                append(license.fechaExpiracion.time)
                append(license.modulosActivos.joinToString(","))
            }
            signature.update(dataToVerify.toByteArray())

            val signatureBytes = Base64.decode(license.firma, Base64.DEFAULT)
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }

    fun checkExpiration(license: License): LicenseStatus {
        val now = System.currentTimeMillis()
        val expiry = license.fechaExpiracion.time
        val graceEnd = expiry + GRACE_PERIOD_MS

        return when {
            now < expiry -> LicenseStatus.Valid
            now < graceEnd -> {
                val hoursRemaining = ((graceEnd - now) / (60 * 60 * 1000)).toInt()
                LicenseStatus.GracePeriod(daysRemaining = hoursRemaining / 24)
            }
            else -> LicenseStatus.Expired
        }
    }

    fun parseLicensePayload(jsonString: String): LicensePayload {
        val json = JSONObject(jsonString)
        return LicensePayload(
            licenseKey = json.getString("license_key"),
            empresaId = json.getString("empresa_id"),
            fechaEmision = java.util.Date(json.getLong("fecha_emision")),
            fechaExpiracion = java.util.Date(json.getLong("fecha_expiracion")),
            modulosActivos = json.getJSONArray("modulos_activos").let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            },
            firma = json.getString("firma")
        )
    }

    private fun parsePublicKey(pem: String): java.security.PublicKey {
        val cleanPem = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
            .replace("\r", "")

        val keyBytes = Base64.decode(cleanPem, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }
}
