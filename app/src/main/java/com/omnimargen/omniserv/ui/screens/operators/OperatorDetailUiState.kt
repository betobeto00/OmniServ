package com.omnimargen.omniserv.ui.screens.operators

import com.omnimargen.omniserv.data.local.dao.OperatorPaymentSummary
import com.omnimargen.omniserv.domain.model.Operator
import com.omnimargen.omniserv.domain.model.Service

data class OperatorDetailUiState(
    val operator: Operator? = null,
    val paymentSummary: OperatorPaymentSummary? = null,
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
