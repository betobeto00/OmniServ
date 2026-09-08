package com.omnimargen.omniserv.ui.screens.activation

import com.omnimargen.omniserv.domain.model.LicenseStatus

data class ActivationUiState(
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val licenseStatus: LicenseStatus? = null,
    val error: String? = null,
    val activationSuccess: Boolean = false
)
