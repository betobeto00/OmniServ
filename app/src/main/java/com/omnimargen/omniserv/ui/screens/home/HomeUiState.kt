package com.omnimargen.omniserv.ui.screens.home

import com.omnimargen.omniserv.domain.model.Service

data class HomeUiState(
    val pendingServices: List<Service> = emptyList(),
    val todayServices: List<Service> = emptyList(),
    val upcomingServices: List<Service> = emptyList(),
    val totalPendingAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)
