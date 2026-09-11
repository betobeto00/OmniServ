package com.omnimargen.omniserv.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TogPlatformApi @Inject constructor() {

    companion object {
        private const val TAG = "TogPlatformApi"
        private const val BASE_URL = "https://tog-platform-production.up.railway.app"
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
        Log.d(TAG, "registerEmpresa called with: nombre=$nombre, pais=$pais, documento=$documento, email=$email")
        try {
            val url = URL("$BASE_URL/api/empresas/register")
            Log.d(TAG, "Connecting to: $url")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val body = JSONObject().apply {
                put("nombre", nombre)
                put("pais", pais)
                put("documento", documento)
                put("email_contacto", email)
                put("device_fingerprint", deviceFingerprint)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            Log.d(TAG, "Register response code: $responseCode, body: $responseBody")

            val json = JSONObject(responseBody)
            if (responseCode in 200..299 && json.optBoolean("success", false)) {
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
            Log.e(TAG, "Error registering empresa", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Exception cause: ${e.cause}")
            LicenseResponse(
                success = false,
                error = "Error de conexión: ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }

    suspend fun checkPaymentStatus(empresaId: String, apiKey: String): LicenseResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/empresas/$empresaId/payment-status")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("x-api-key", apiKey)

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            Log.d(TAG, "Payment status response code: $responseCode, body: $responseBody")

            val json = JSONObject(responseBody)
            LicenseResponse(
                success = responseCode in 200..299,
                paymentConfirmed = json.optBoolean("payment_confirmed", false),
                error = if (responseCode !in 200..299) json.optString("error") else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking payment status", e)
            LicenseResponse(
                success = false,
                paymentConfirmed = false,
                error = "Error de conexión: ${e.message}"
            )
        }
    }

    /**
     * Descarga la licencia activa de la empresa. Envía SIEMPRE el fingerprint del
     * dispositivo: el servidor vincula la licencia al primer teléfono que la
     * reclama y rechaza (DEVICE_MISMATCH) a cualquier otro.
     */
    suspend fun getLicense(empresaId: String, apiKey: String, deviceFingerprint: String): LicenseResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/empresas/$empresaId/licencia")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("x-api-key", apiKey)
            connection.setRequestProperty("x-device-fingerprint", deviceFingerprint)

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            Log.d(TAG, "Get license response code: $responseCode, body: $responseBody")

            val json = JSONObject(responseBody)
            if (responseCode in 200..299 && json.optBoolean("success", false)) {
                // Formato de tog-platform: { success, licencia: { id, cliente, expira,
                // version, machineId, modules, emitida, firma } }
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
            Log.e(TAG, "Error getting license", e)
            LicenseResponse(
                success = false,
                error = "Error de conexión: ${e.message}"
            )
        }
    }

    suspend fun getServerTime(): Long = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/time")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }

            val json = JSONObject(responseBody)
            json.optLong("server_time", System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting server time", e)
            System.currentTimeMillis()
        }
    }

    suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/health")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            val responseBody = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }

            val json = JSONObject(responseBody)
            responseCode in 200..299 && json.optBoolean("ok", false)
        } catch (e: Exception) {
            Log.e(TAG, "Health check failed", e)
            false
        }
    }

    suspend fun loginUser(email: String, password: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/auth/login")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            Log.d(TAG, "Login response code: $responseCode, body: $responseBody")

            val json = JSONObject(responseBody)
            if (responseCode in 200..299 && json.optBoolean("success", false)) {
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
            Log.e(TAG, "Error logging in", e)
            AuthResponse(
                success = false,
                error = "Error de conexión: ${e.message ?: e.javaClass.simpleName}"
            )
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
            val url = URL("$BASE_URL/api/auth/register")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val body = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("nombre", nombre)
                put("pais", pais)
                put("documento", documento)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            Log.d(TAG, "Register response code: $responseCode, body: $responseBody")

            val json = JSONObject(responseBody)
            if (responseCode in 200..299 && json.optBoolean("success", false)) {
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
            Log.e(TAG, "Error registering user", e)
            AuthResponse(
                success = false,
                error = "Error de conexión: ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }

    suspend fun getUserProfile(token: String): AuthResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/user/profile")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $token")

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
            }

            Log.d(TAG, "Profile response code: $responseCode, body: $responseBody")

            val json = JSONObject(responseBody)
            if (responseCode in 200..299 && json.optBoolean("success", false)) {
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
            Log.e(TAG, "Error getting profile", e)
            AuthResponse(
                success = false,
                error = "Error de conexión: ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }
}
