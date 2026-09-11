package com.omnimargen.omniserv.ui.screens.activation

import com.omnimargen.omniserv.domain.model.LicenseStatus

data class ActivationUiState(
    // Auth mode
    val isLoginMode: Boolean = false, // false = registro, true = login
    val authStep: AuthStep = AuthStep.EMAIL,
    // User data
    val nombre: String = "",
    val pais: String = "",
    val documento: String = "",
    val email: String = "",
    val password: String = "",
    // Auth state
    val authToken: String? = null,
    val userId: Int? = null,
    // Empresa / registration state
    val isRegistered: Boolean = false,
    val empresaId: String? = null,
    val apiKey: String? = null,
    // Payment state
    val paymentPending: Boolean = false,
    val paymentConfirmed: Boolean = false,
    // General state
    val isLoading: Boolean = false,
    val licenseStatus: LicenseStatus? = null,
    val error: String? = null,
    val activationSuccess: Boolean = false,
    val currentStep: Int = 1 // 1: Auth, 2: Payment, 3: Activated
)

enum class AuthStep {
    EMAIL,      // Ingresar email (verificar si existe)
    PASSWORD,   // Ingresar clave (login o registro)
    REGISTER    // Completar datos (solo si es nuevo)
}
