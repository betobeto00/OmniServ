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

    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun updatePais(pais: String) {
        _uiState.update { it.copy(pais = pais) }
    }

    fun updateDocumento(documento: String) {
        _uiState.update { it.copy(documento = documento) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun register() {
        val state = _uiState.value

        // Validar inputs
        val validation = SecurityUtils.validateAll(state.nombre, state.pais, state.documento, state.email)
        if (!validation.isValid) {
            _uiState.update { it.copy(error = validation.error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Verificar hora del servidor contra la del dispositivo
                val serverTime = togPlatformApi.getServerTime()
                val localTime = System.currentTimeMillis()
                val timeDrift = SecurityUtils.validateTimeDrift(localTime, serverTime)
                if (!timeDrift.isValid) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = timeDrift.error
                        )
                    }
                    return@launch
                }

                val deviceFingerprint = licenseGenerator.generateDeviceFingerprint()

                // Sanitizar inputs antes de enviar
                val response = togPlatformApi.registerEmpresa(
                    nombre = SecurityUtils.sanitizeInput(state.nombre),
                    pais = state.pais.trim().uppercase(),
                    documento = SecurityUtils.sanitizeInput(state.documento),
                    email = state.email.trim().lowercase(),
                    deviceFingerprint = deviceFingerprint
                )

                if (response.success) {
                    if (response.alreadyRegistered) {
                        // Usuario ya registrado, verificar estado de pago
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRegistered = true,
                                empresaId = response.empresaId,
                                apiKey = response.apiKey,
                                currentStep = 2,
                                paymentPending = true
                            )
                        }
                        // Verificar si ya tiene pago confirmado
                        startPaymentPolling(response.empresaId!!, response.apiKey!!)
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRegistered = true,
                                empresaId = response.empresaId,
                                apiKey = response.apiKey,
                                currentStep = 2,
                                paymentPending = true
                            )
                        }
                        // Start polling for payment confirmation
                        startPaymentPolling(response.empresaId!!, response.apiKey!!)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.error ?: "Error al registrar"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    private fun startPaymentPolling(empresaId: String, apiKey: String) {
        viewModelScope.launch {
            var attempts = 0
            val maxAttempts = 60 // 5 minutes with 5-second intervals

            while (attempts < maxAttempts && _uiState.value.paymentPending) {
                delay(5000) // Poll every 5 seconds
                attempts++

                try {
                    val response = togPlatformApi.checkPaymentStatus(empresaId, apiKey)
                    if (response.paymentConfirmed) {
                        // Payment confirmed! Activate license (vinculada a ESTE teléfono)
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
                // Fechas reales de la licencia firmada por tog-platform
                // ("emitida" ISO 8601, "expira" YYYY-MM-DD), no valores inventados.
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
                // Incluye DEVICE_MISMATCH: la licencia ya está activada en otro
                // teléfono (el mensaje del servidor ya es amigable para el usuario).
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
                    error = "Error de activación: ${e.message}"
                )
            }
        }
    }

    /** "2026-09-08T15:43:00.000Z" → Date (UTC; se ignora la fracción y la zona). */
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

    /** "2026-12-31" → Date a medianoche UTC de ese día. */
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
        // resolveActivity valida que el deep link sea resoluble por la app de
        // Crixto (además de la visibilidad de paquete declarada en <queries>).
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getCrixtoDeepLink())).apply {
            setPackage("crixto.pay")
        }
        return intent.resolveActivity(context.packageManager) != null
    }

    fun openCrixto(context: Context) {
        val intent = if (isCrixtoAppInstalled(context)) {
            // Abrir app de Crixto
            Intent(Intent.ACTION_VIEW, Uri.parse(getCrixtoDeepLink())).apply {
                setPackage("crixto.pay")
            }
        } else {
            // Abrir en navegador
            Intent(Intent.ACTION_VIEW, Uri.parse(getCrixtoUrl()))
        }
        context.startActivity(intent)
    }

    /**
     * Activa el período de prueba de 7 días: persiste la fecha de inicio para
     * que el trial sobreviva al cierre de la app y navega al dashboard.
     */
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
