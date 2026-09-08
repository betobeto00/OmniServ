package com.omnimargen.omniserv.ui.screens.activation

import com.omnimargen.omniserv.domain.model.LicenseStatus

data class ActivationUiState(
    // User data for registration
    val nombre: String = "",
    val pais: String = "Venezuela",
    val documento: String = "",
    val email: String = "",
    // Registration state
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
    val currentStep: Int = 1 // 1: Register, 2: Payment, 3: Activated
)
