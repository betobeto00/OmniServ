package com.omnimargen.omniserv.ui.screens.services

import com.omnimargen.omniserv.domain.model.Client
import com.omnimargen.omniserv.domain.model.Operator
import com.omnimargen.omniserv.domain.model.Service
import com.omnimargen.omniserv.domain.model.ServiceStatus
import com.omnimargen.omniserv.domain.model.ServiceType

data class ServiceUiState(
    val services: List<Service> = emptyList(),
    val clients: List<Client> = emptyList(),
    val serviceTypes: List<ServiceType> = emptyList(),
    val operators: List<Operator> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterStatus: ServiceStatus? = null
)
