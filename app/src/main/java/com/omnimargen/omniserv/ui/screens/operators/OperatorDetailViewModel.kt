package com.omnimargen.omniserv.ui.screens.operators

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.domain.usecase.operator.GetOperatorsUseCase
import com.omnimargen.omniserv.domain.usecase.operator.GetOperatorPaymentSummaryUseCase
import com.omnimargen.omniserv.domain.usecase.operator.GetServicesByOperatorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OperatorDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOperatorsUseCase: GetOperatorsUseCase,
    private val getOperatorPaymentSummaryUseCase: GetOperatorPaymentSummaryUseCase,
    private val getServicesByOperatorUseCase: GetServicesByOperatorUseCase
) : ViewModel() {

    private val operatorId: Long = savedStateHandle["operatorId"] ?: -1L

    private val _uiState = MutableStateFlow(OperatorDetailUiState())
    val uiState: StateFlow<OperatorDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getOperatorsUseCase().collect { operators ->
                val operator = operators.find { it.id == operatorId }
                _uiState.update { it.copy(operator = operator) }
            }
        }

        viewModelScope.launch {
            getOperatorPaymentSummaryUseCase().collect { summaries ->
                val summary = summaries.find { it.operatorId == operatorId }
                _uiState.update { it.copy(paymentSummary = summary, isLoading = false) }
            }
        }

        viewModelScope.launch {
            getServicesByOperatorUseCase(operatorId).collect { services ->
                _uiState.update { it.copy(services = services) }
            }
        }
    }
}
