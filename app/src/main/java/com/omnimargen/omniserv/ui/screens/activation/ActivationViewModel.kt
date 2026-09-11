package com.omnimargen.omniserv.ui.screens.activation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.data.remote.TogPlatformApi
import com.omnimargen.omniserv.domain.model.License
import com.omnimargen.omniserv.domain.model.LicenseStatus
import com.omnimargen.omniserv.domain.usecase.license.GetLicenseStatusUseCase
import com.omnimargen.omniserv.license.LicenseGenerator
import com.omnimargen.omniserv.data.repository.LicenseRepository
import com.omnimargen.omniserv.util.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ActivationViewModel @Inject constructor(
    private val getLicenseStatusUseCase: GetLicenseStatusUseCase,
    private val licenseGenerator: LicenseGenerator,
    private val licenseRepository: LicenseRepository,
    private val togPlatformApi: TogPlatformApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    companion object {
        const val CRIXTO_BUTTON_KEY = "9C637B7513908464148FA512FAB1A06F"
        const val CRIXTO_BUY_URL = "https://crixto.io/cgi/buy-now?button_key=$CRIXTO_BUTTON_KEY"
        const val CRIXTO_APP_SCHEME = "https://app-prod.crixto.org/app"
    }

    init {
        checkLicenseStatus()
    }

    private fun checkLicenseStatus() {
        viewModelScope.launch {
            val status = getLicenseStatusUseCase()
            _uiState.update { it.copy(licenseStatus = status) }
        }
    }

    // --- Auth flow ---

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun updatePais(pais: String) {
        _uiState.update { it.copy(pais = pais) }
    }

    fun updateDocumento(documento: String) {
        _uiState.update { it.copy(documento = documento) }
    }

    /**
     * Paso 1: Verificar si el email ya esta registrado.
     * Si existe → ir a login (pedir clave).
     * Si no → ir a registro (pedir clave + datos).
     */
    fun checkEmail() {
        val email = _uiState.value.email.trim().lowercase()
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(error = "Ingresa un email valido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Intentar login con password vacio para detectar si el email existe
            // (el servidor retorna "Credenciales incorrectas" si no existe o si la clave es wrong)
            // Mejor: hacer un registro rapido y si falla con "email already taken" → login
            // O simplemente asumir que si el usuario tiene cuenta, ingresa clave.
            // Flujo simplificado: siempre preguntar clave, despues decidir.

            // Simulacion rapida: intentar login con string vacio
            val loginResult = togPlatformApi.loginUser(email, "__check__")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoginMode = false, // Por defecto asumimos registro
                    authStep = AuthStep.PASSWORD
                )
            }
        }
    }

    /**
     * Paso 2: El usuario ingresa su clave.
     * Intentamos login. Si falla, intentamos registro.
     */
    fun submitPassword() {
        val state = _uiState.value
        val email = state.email.trim().lowercase()
        val password = state.password

        if (password.length < 6) {
            _uiState.update { it.copy(error = "La clave debe tener al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Intentar login
            val loginResult = togPlatformApi.loginUser(email, password)
            if (loginResult.success && loginResult.token != null) {
                // Login exitoso
                handleAuthSuccess(loginResult)
                return@launch
            }

            // Login fallo → puede que el usuario no exista, intentar registro
            // Solo si el error indica credenciales incorrectas (no "email taken")
            if (loginResult.error?.contains("incorrectas", ignoreCase = true) == true ||
                loginResult.error?.contains("incorrect", ignoreCase = true) == true) {

                // Si estamos en modo login y fallo, mostrar error
                if (state.isLoginMode) {
                    _uiState.update {
                        it.copy(isLoading = false, error = loginResult.error ?: "Credenciales incorrectas")
                    }
                    return@launch
                }

                // Intentar registro (el servidor creara la empresa + user)
                val registerResult = togPlatformApi.registerUser(
                    email = email,
                    password = password,
                    nombre = state.nombre.ifBlank { email.substringBefore("@") },
                    pais = state.pais,
                    documento = state.documento
                )
                if (registerResult.success && registerResult.token != null) {
                    handleAuthSuccess(registerResult)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = registerResult.error ?: "Error al registrar"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = loginResult.error ?: "Error de autenticacion")
                }
            }
        }
    }

    private suspend fun handleAuthSuccess(result: TogPlatformApi.AuthResponse) {
        val empresaId = result.empresaId?.toString()

        _uiState.update {
            it.copy(
                isLoading = false,
                authToken = result.token,
                userId = result.userId,
                nombre = result.nombre ?: it.nombre,
                empresaId = empresaId,
                isRegistered = empresaId != null,
                currentStep = 2,
                paymentPending = true
            )
        }

        // Registrar empresa si no existe (para el flujo de licencia device-based)
        if (empresaId == null) {
            registerEmpresaForLicense()
        } else {
            // Ya tiene empresa, verificar pago
            startPaymentPolling(empresaId)
        }
    }

    /**
     * Registra la empresa en el flujo device-based (para obtener api_key).
     * Esto es necesario porque la licencia se vincula al dispositivo via api_key.
     */
    private suspend fun registerEmpresaForLicense() {
        val state = _uiState.value
        val deviceFingerprint = licenseGenerator.generateDeviceFingerprint()

        try {
            val response = togPlatformApi.registerEmpresa(
                nombre = state.nombre.ifBlank { state.email.substringBefore("@") },
                pais = state.pais.trim().uppercase(),
                documento = state.documento.ifBlank { "N/A" },
                email = state.email.trim().lowercase(),
                deviceFingerprint = deviceFingerprint
            )

            if (response.success) {
                val newEmpresaId = response.empresaId
                val newApiKey = response.apiKey
                _uiState.update {
                    it.copy(
                        empresaId = newEmpresaId,
                        apiKey = newApiKey,
                        paymentPending = true
                    )
                }
                if (newEmpresaId != null) {
                    startPaymentPolling(newEmpresaId)
                }
            }
        } catch (e: Exception) {
            // No bloquear el flujo si falla el registro de empresa
        }
    }

    fun setLoginMode() {
        _uiState.update {
            it.copy(
                isLoginMode = true,
                authStep = AuthStep.PASSWORD,
                error = null
            )
        }
    }

    fun setRegisterMode() {
        _uiState.update {
            it.copy(
                isLoginMode = false,
                authStep = AuthStep.REGISTER,
                error = null
            )
        }
    }

    fun goToEmailStep() {
        _uiState.update {
            it.copy(
                authStep = AuthStep.EMAIL,
                password = "",
                error = null,
                isLoginMode = false
            )
        }
    }

    // --- Payment flow ---

    private fun startPaymentPolling(empresaId: String) {
        val apiKey = _uiState.value.apiKey ?: return

        viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 60

            while (attempts < maxAttempts && _uiState.value.paymentPending) {
                delay(5000)
                attempts++

                try {
                    val response = togPlatformApi.checkPaymentStatus(empresaId, apiKey)
                    if (response.paymentConfirmed) {
                        val deviceFingerprint = licenseGenerator.generateDeviceFingerprint()
                        activateLicense(empresaId, apiKey, deviceFingerprint)
                        return@launch
                    }
                } catch (e: Exception) {
                    // Continue polling
                }
            }
        }
    }

    private suspend fun activateLicense(empresaId: String, apiKey: String, deviceFingerprint: String) {
        try {
            val licenseResponse = togPlatformApi.getLicense(empresaId, apiKey, deviceFingerprint)

            if (licenseResponse.success && licenseResponse.firma != null) {
                val ahora = Date()
                val license = License(
                    licenseKey = licenseResponse.licenseKey ?: apiKey,
                    empresaId = empresaId,
                    fechaEmision = parseIsoDate(licenseResponse.emitida, ahora),
                    fechaExpiracion = parseDayDate(licenseResponse.expira, ahora),
                    modulosActivos = licenseResponse.modulos ?: listOf("omniserv"),
                    firma = licenseResponse.firma
                )
                licenseRepository.saveLicense(license)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        paymentPending = false,
                        paymentConfirmed = true,
                        currentStep = 3,
                        activationSuccess = true,
                        licenseStatus = LicenseStatus.Valid,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = licenseResponse.error ?: "Error al activar licencia"
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Error de activacion: ${e.message}"
                )
            }
        }
    }

    private fun parseIsoDate(value: String?, fallback: Date): Date {
        if (value.isNullOrBlank()) return fallback
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
                isLenient = false
            }.parse(value) ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }

    private fun parseDayDate(value: String?, fallback: Date): Date {
        if (value.isNullOrBlank()) return fallback
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
                isLenient = false
            }.parse(value) ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }

    fun getCrixtoUrl(): String {
        val empresaId = _uiState.value.empresaId
        val successUrl = if (empresaId != null) {
            "https://tog-platform-production.up.railway.app/api/payment/confirm?empresa_id=$empresaId"
        } else {
            ""
        }
        return "$CRIXTO_BUY_URL${if (successUrl.isNotEmpty()) "&success_url=$successUrl" else ""}"
    }

    fun getCrixtoDeepLink(): String {
        val empresaId = _uiState.value.empresaId
        return if (empresaId != null) {
            "$CRIXTO_APP_SCHEME?button_key=$CRIXTO_BUTTON_KEY&success_url=https://tog-platform-production.up.railway.app/api/payment/confirm?empresa_id=$empresaId"
        } else {
            "$CRIXTO_APP_SCHEME?button_key=$CRIXTO_BUTTON_KEY"
        }
    }

    fun isCrixtoAppInstalled(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getCrixtoDeepLink())).apply {
            setPackage("crixto.pay")
        }
        return intent.resolveActivity(context.packageManager) != null
    }

    fun openCrixto(context: Context) {
        val intent = if (isCrixtoAppInstalled(context)) {
            Intent(Intent.ACTION_VIEW, Uri.parse(getCrixtoDeepLink())).apply {
                setPackage("crixto.pay")
            }
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(getCrixtoUrl()))
        }
        context.startActivity(intent)
    }

    fun skipTrial() {
        licenseRepository.startTrialPeriod()
        _uiState.update {
            it.copy(
                currentStep = 3,
                activationSuccess = true,
                licenseStatus = LicenseStatus.TrialPeriod(7)
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
