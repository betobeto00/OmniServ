package com.omnimargen.omniserv.ui.screens.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.domain.model.LicenseStatus
import com.omnimargen.omniserv.domain.usecase.license.ActivateLicenseUseCase
import com.omnimargen.omniserv.domain.usecase.license.GetLicenseStatusUseCase
import com.omnimargen.omniserv.license.LicenseGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val activateLicenseUseCase: ActivateLicenseUseCase,
    private val licenseGenerator: LicenseGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    init {
        checkLicenseStatus()
    }

    private fun checkLicenseStatus() {
        viewModelScope.launch {
            val status = getLicenseStatusUseCase()
            _uiState.update { it.copy(licenseStatus = status) }
        }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey) }
    }

    fun activate() {
        val apiKey = _uiState.value.apiKey.trim()
        if (apiKey.isBlank()) {
            _uiState.update { it.copy(error = "Ingrese una API key válida") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val deviceFingerprint = licenseGenerator.generateDeviceFingerprint()

                val expiryDate = Date(System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L))

                val status = activateLicenseUseCase(
                    licenseKey = apiKey,
                    empresaId = deviceFingerprint.take(8),
                    modulosActivos = listOf("omniserv"),
                    fechaExpiracion = expiryDate
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activationSuccess = status is LicenseStatus.Valid,
                        licenseStatus = status,
                        error = if (status is LicenseStatus.InvalidSignature)
                            "Licencia inválida para OmniServ" else null
                    )
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
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
