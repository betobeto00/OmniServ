package com.omnimargen.omniserv.ui.screens.history

import com.omnimargen.omniserv.domain.model.Service

data class HistoryUiState(
    val completedServices: List<Service> = emptyList(),
    val totalRevenue: Double = 0.0,
    val operatorBreakdown: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
