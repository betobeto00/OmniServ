package com.omnimargen.omniserv.data.remote

import com.omnimargen.omniserv.BuildConfig
import com.omnimargen.omniserv.data.repository.LicenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TogPlatformApi @Inject constructor(
    private val licenseRepository: LicenseRepository
) {

    companion object {
        private const val TAG = "TogPlatformApi"
        private const val BASE_URL = "https://tog-platform-production.up.railway.app"

        private const val CONNECT_TIMEOUT_MS = 10_000L
        private const val READ_TIMEOUT_MS = 30_000L
        private const val WRITE_TIMEOUT_MS = 15_000L
        private const val MAX_RETRIES = 3
        private const val BASE_BACKOFF_MS = 1_000L
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(false)

        configureCertificatePinning(builder)

        builder.build()
    }

    private fun configureCertificatePinning(builder: OkHttpClient.Builder) {
        try {
            val certificatePinner = okhttp3.CertificatePinner.Builder()
                .add(
                    "tog-platform-production.up.railway.app",
                    "sha256/ErIMn03cxhS+PK7UKUcSOY5pqegEhCn8Xvw4k3LqAnw=",
                )
                .build()
            builder.certificatePinner(certificatePinner)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.w(TAG, "Certificate pinning setup failed: ${e.message}")
        }
    }

    private suspend fun executeWithRetry(
        retries: Int = MAX_RETRIES,
        block: suspend () -> JSONObject,
    ): JSONObject {
        var lastException: Exception? = null
        repeat(retries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (attempt < retries - 1) {
                    val backoff = BASE_BACKOFF_MS * (1L shl attempt)
                    if (BuildConfig.DEBUG) android.util.Log.d(TAG, "Retry ${attempt + 1}/$retries after ${backoff}ms")
                    Thread.sleep(backoff)
                }
            }
        }
        throw lastException ?: Exception("Max retries exceeded")
    }

    private fun postJson(url: String, body: JSONObject, headers: Map<String, String> = emptyMap()): JSONObject {
        val request = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody(jsonMediaType))
            .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: "{}"
            return JSONObject(responseBody)
        }
    }

    private fun getJson(url: String, headers: Map<String, String> = emptyMap()): JSONObject {
        val request = Request.Builder()
            .url(url)
            .get()
            .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: "{}"
            return JSONObject(responseBody)
        }
    }

    data class LicenseResponse(
        val success: Boolean,
        val licenseKey: String? = null,
        val empresaId: String? = null,
        val apiKey: String? = null,
        val modulos: List<String>? = null,
        val expira: String? = null,
        val emitida: String? = null,
        val firma: String? = null,
        val error: String? = null,
        val errorCode: String? = null,
        val paymentConfirmed: Boolean = false,
        val alreadyRegistered: Boolean = false
    )

    data class AuthResponse(
        val success: Boolean,
        val token: String? = null,
        val userId: Int? = null,
        val email: String? = null,
        val nombre: String? = null,
        val empresaId: Int? = null,
        val paymentStatus: String? = null,
        val error: String? = null
    )

    suspend fun registerEmpresa(
        nombre: String,
        pais: String,
        documento: String,
        email: String,
        deviceFingerprint: String
    ): LicenseResponse = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("nombre", nombre)
                put("pais", pais)
                put("documento", documento)
                put("email_contacto", email)
                put("device_fingerprint", deviceFingerprint)
            }

            val json = executeWithRetry {
                postJson("$BASE_URL/api/empresas/register", body)
            }

            if (json.optBoolean("success", false)) {
                val data = json.getJSONObject("data")
                LicenseResponse(
                    success = true,
                    empresaId = data.optString("id"),
                    apiKey = data.optString("api_key"),
                    alreadyRegistered = json.optBoolean("already_registered", false),
                    error = null
                )
            } else {
                LicenseResponse(
                    success = false,
                    error = json.optString("message").ifEmpty { json.optString("error", "Error desconocido") },
                    errorCode = json.optString("code").ifEmpty { null }
                )
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.e(TAG, "Error registering empresa: ${e.message}")
            LicenseResponse(success = false, error = "Error de conexión")
        }
    }

    suspend fun checkPaymentStatus(empresaId: String, apiKey: String): LicenseResponse = withContext(Dispatchers.IO) {
        try {
            val json = executeWithRetry {
                getJson(
                    "$BASE_URL/api/empresas/$empresaId/payment-status",
                    mapOf("x-api-key" to apiKey),
                )
            }

            LicenseResponse(
                success = true,
                paymentConfirmed = json.optBoolean("payment_confirmed", false),
                error = null
            )
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.e(TAG, "Error checking payment: ${e.message}")
            LicenseResponse(success = false, paymentConfirmed = false, error = "Error de conexión")
        }
    }

    suspend fun getLicense(empresaId: String, apiKey: String, deviceFingerprint: String): LicenseResponse = withContext(Dispatchers.IO) {
        try {
            val json = executeWithRetry {
                getJson(
                    "$BASE_URL/api/empresas/$empresaId/licencia",
                    mapOf("x-api-key" to apiKey, "x-device-fingerprint" to deviceFingerprint),
                )
            }

            if (json.optBoolean("success", false)) {
                val licencia = json.getJSONObject("licencia")
                LicenseResponse(
                    success = true,
                    licenseKey = licencia.optString("id"),
                    modulos = licencia.optJSONArray("modules")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    },
                    expira = licencia.optString("expira"),
                    emitida = licencia.optString("emitida"),
                    firma = licencia.optString("firma"),
                    error = null
                )
            } else {
                LicenseResponse(
                    success = false,
                    error = json.optString("message").ifEmpty { json.optString("error", "Error desconocido") },
                    errorCode = json.optString("code").ifEmpty { null }
                )
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.e(TAG, "Error getting license: ${e.message}")
            LicenseResponse(success = false, error = "Error de conexión")
        }
    }

    suspend fun getServerTime(): Long = withContext(Dispatchers.IO) {
        try {
            val json = executeWithRetry { getJson("$BASE_URL/api/time") }
            json.optLong("server_time", System.currentTimeMillis())
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.e(TAG, "Error getting server time: ${e.message}")
            System.currentTimeMillis()
        }
    }

    suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = getJson("$BASE_URL/api/health")
            json.optBoolean("ok", false)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.e(TAG, "Health check failed: ${e.message}")
            false
        }
    }

    suspend fun checkEmailExists(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("email", email)
            }
            val json = postJson("$BASE_URL/api/auth/check-email", body)
            json.optBoolean("exists", false)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.w(TAG, "Error checking email: ${e.message}")
            false
        }
    }

    suspend fun loginUser(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val json = executeWithRetry {
                postJson("$BASE_URL/api/auth/login", body)
            }

            if (json.optBoolean("success", false)) {
                val user = json.getJSONObject("user")
                AuthResponse(
                    success = true,
                    token = json.getString("token"),
                    userId = user.optInt("id"),
                    email = user.optString("email"),
                    nombre = user.optString("nombre"),
                    empresaId = user.optInt("empresa_id").takeIf { it > 0 },
                    paymentStatus = user.optString("payment_status")
                )
            } else {
                AuthResponse(
                    success = false,
                    error = json.optString("message").ifEmpty { json.optString("error", "Credenciales incorrectas") }
                )
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.e(TAG, "Error logging in: ${e.message}")
            AuthResponse(success = false, error = "Error de conexión")
        }
    }

    suspend fun registerUser(
        email: String,
        password: String,
        nombre: String,
        pais: String,
        documento: String
    ): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("nombre", nombre)
                put("pais", pais)
                put("documento", documento)
            }

            val json = executeWithRetry {
                postJson("$BASE_URL/api/auth/register", body)
            }

            if (json.optBoolean("success", false)) {
                val user = json.getJSONObject("user")
                AuthResponse(
                    success = true,
                    token = json.getString("token"),
                    userId = user.optInt("id"),
                    email = user.optString("email"),
                    nombre = user.optString("nombre"),
                    empresaId = user.optInt("empresa_id").takeIf { it > 0 }
                )
            } else {
                AuthResponse(
                    success = false,
                    error = json.optString("message").ifEmpty { json.optString("error", "Error al registrar") }
                )
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.e(TAG, "Error registering: ${e.message}")
            AuthResponse(success = false, error = "Error de conexión")
        }
    }

    suspend fun getUserProfile(token: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val json = executeWithRetry {
                getJson(
                    "$BASE_URL/api/user/profile",
                    mapOf("Authorization" to "Bearer $token"),
                )
            }

            if (json.optBoolean("success", false)) {
                val user = json.getJSONObject("user")
                val empresa = json.optJSONObject("empresa")
                AuthResponse(
                    success = true,
                    userId = user.optInt("id"),
                    email = user.optString("email"),
                    nombre = user.optString("nombre"),
                    empresaId = empresa?.optInt("id")?.takeIf { it > 0 },
                    paymentStatus = empresa?.optString("payment_status")
                )
            } else {
                AuthResponse(
                    success = false,
                    error = json.optString("message").ifEmpty { json.optString("error", "Error al obtener perfil") }
                )
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) android.util.Log.e(TAG, "Error getting profile: ${e.message}")
            AuthResponse(success = false, error = "Error de conexión")
        }
    }

    suspend fun getEmpresaIdByEmail(email: String): String? = withContext(Dispatchers.IO) {
        try {
            val token = licenseRepository.getAuthToken() ?: return@withContext null
            val profile = getUserProfile(token)
            profile.empresaId?.toString()
        } catch (e: Exception) {
            null
        }
    }
}
