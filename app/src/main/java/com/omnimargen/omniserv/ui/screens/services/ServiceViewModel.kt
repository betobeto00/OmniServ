package com.omnimargen.omniserv.ui.screens.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.domain.model.Service
import com.omnimargen.omniserv.domain.model.ServiceStatus
import com.omnimargen.omniserv.domain.usecase.client.GetClientsUseCase
import com.omnimargen.omniserv.domain.usecase.operator.GetOperatorsUseCase
import com.omnimargen.omniserv.domain.usecase.service.AddServiceUseCase
import com.omnimargen.omniserv.domain.usecase.service.DeleteServiceUseCase
import com.omnimargen.omniserv.domain.usecase.service.GetServicesUseCase
import com.omnimargen.omniserv.domain.usecase.service.UpdateServiceUseCase
import com.omnimargen.omniserv.domain.usecase.serviceType.GetAllServiceTypesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val getServicesUseCase: GetServicesUseCase,
    private val addServiceUseCase: AddServiceUseCase,
    private val updateServiceUseCase: UpdateServiceUseCase,
    private val deleteServiceUseCase: DeleteServiceUseCase,
    private val getClientsUseCase: GetClientsUseCase,
    private val getAllServiceTypesUseCase: GetAllServiceTypesUseCase,
    private val getOperatorsUseCase: GetOperatorsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getServicesUseCase().collect { services ->
                _uiState.update { it.copy(services = services, isLoading = false) }
            }
        }
        viewModelScope.launch {
            getClientsUseCase().collect { clients ->
                _uiState.update { it.copy(clients = clients) }
            }
        }
        viewModelScope.launch {
            getAllServiceTypesUseCase().collect { types ->
                _uiState.update { it.copy(serviceTypes = types) }
            }
        }
        viewModelScope.launch {
            getOperatorsUseCase().collect { operators ->
                _uiState.update { it.copy(operators = operators) }
            }
        }
    }

    fun setFilterStatus(status: ServiceStatus?) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    fun addService(service: Service) {
        viewModelScope.launch {
            addServiceUseCase(service)
        }
    }

    fun updateService(service: Service) {
        viewModelScope.launch {
            updateServiceUseCase(service)
        }
    }

    fun deleteService(service: Service) {
        viewModelScope.launch {
            deleteServiceUseCase(service)
        }
    }

    fun updateServiceStatus(service: Service, newStatus: ServiceStatus) {
        viewModelScope.launch {
            updateServiceUseCase(service.copy(estado = newStatus))
        }
    }
}
