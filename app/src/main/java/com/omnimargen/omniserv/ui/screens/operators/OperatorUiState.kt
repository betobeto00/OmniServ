package com.omnimargen.omniserv.ui.screens.operators

import com.omnimargen.omniserv.data.local.dao.OperatorPaymentSummary
import com.omnimargen.omniserv.domain.model.Operator

data class OperatorUiState(
    val operators: List<Operator> = emptyList(),
    val paymentSummary: List<OperatorPaymentSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
