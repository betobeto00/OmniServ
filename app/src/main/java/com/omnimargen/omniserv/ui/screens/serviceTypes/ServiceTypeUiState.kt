package com.omnimargen.omniserv.ui.screens.serviceTypes

import com.omnimargen.omniserv.domain.model.ServiceType

data class ServiceTypeUiState(
    val serviceTypes: List<ServiceType> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
