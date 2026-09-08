package com.omnimargen.omniserv.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.domain.model.ServiceStatus
import com.omnimargen.omniserv.domain.usecase.service.GetServicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getServicesUseCase: GetServicesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getServicesUseCase().collect { services ->
                val completed = services.filter {
                    it.estado == ServiceStatus.REALIZADO || it.estado == ServiceStatus.CANCELADO
                }
                val totalRevenue = completed
                    .filter { it.estado == ServiceStatus.REALIZADO }
                    .sumOf { it.monto }

                val operatorBreakdown = mutableMapOf<String, Double>()
                completed.filter { it.estado == ServiceStatus.REALIZADO }.forEach { service ->
                    service.operarios.forEach { op ->
                        operatorBreakdown[op.operatorNombre] =
                            (operatorBreakdown[op.operatorNombre] ?: 0.0) + op.montoPago
                    }
                }

                _uiState.update {
                    it.copy(
                        completedServices = completed,
                        totalRevenue = totalRevenue,
                        operatorBreakdown = operatorBreakdown,
                        isLoading = false
                    )
                }
            }
        }
    }
}
