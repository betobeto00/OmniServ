package com.omnimargen.omniserv.license

import android.util.Base64
import android.util.Log
import com.omnimargen.omniserv.BuildConfig
import com.omnimargen.omniserv.domain.model.License
import com.omnimargen.omniserv.domain.model.LicenseStatus
import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

sealed class SignatureResult {
    data object Valid : SignatureResult()
    data object InvalidSignature : SignatureResult()
    data object MalformedData : SignatureResult()
    data object InvalidKey : SignatureResult()
    data class Error(val message: String) : SignatureResult()
}

@Singleton
class LicenseValidator @Inject constructor() {

    companion object {
        private const val TAG = "LicenseValidator"
        private const val GRACE_PERIOD_HOURS = 48
        private const val GRACE_PERIOD_MS = GRACE_PERIOD_HOURS * 60 * 60 * 1000L
    }

    fun validateSignature(license: License, publicKeyPem: String): SignatureResult {
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
            val isValid = signature.verify(signatureBytes)
            if (isValid) SignatureResult.Valid else SignatureResult.InvalidSignature
        } catch (e: java.security.spec.InvalidKeySpecException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Invalid public key: ${e.message}")
            SignatureResult.InvalidKey
        } catch (e: java.security.SignatureException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Signature verification error: ${e.message}")
            SignatureResult.MalformedData
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Signature validation error: ${e.message}")
            SignatureResult.Error(e.message ?: "Error desconocido")
        }
    }

    fun checkExpiration(license: License): LicenseStatus {
        val now = System.currentTimeMillis()
        val expiry = license.fechaExpiracion.time
        val graceEnd = expiry + GRACE_PERIOD_MS

        return when {
            now < expiry -> LicenseStatus.Valid
            now < graceEnd -> {
                val daysRemaining = kotlin.math.ceil((graceEnd - now).toDouble() / (24 * 60 * 60 * 1000)).toInt()
                LicenseStatus.GracePeriod(daysRemaining = daysRemaining)
            }
            else -> LicenseStatus.Expired
        }
    }

    fun validate(license: License, publicKeyPem: String): LicenseStatus {
        return when (val sigResult = validateSignature(license, publicKeyPem)) {
            is SignatureResult.Valid -> checkExpiration(license)
            is SignatureResult.InvalidKey -> LicenseStatus.InvalidSignature
            is SignatureResult.InvalidSignature -> LicenseStatus.InvalidSignature
            is SignatureResult.MalformedData -> LicenseStatus.InvalidSignature
            is SignatureResult.Error -> LicenseStatus.InvalidSignature
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
