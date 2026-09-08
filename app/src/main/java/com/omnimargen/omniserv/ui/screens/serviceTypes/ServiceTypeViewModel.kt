package com.omnimargen.omniserv.ui.screens.serviceTypes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.domain.model.ServiceType
import com.omnimargen.omniserv.domain.usecase.serviceType.AddServiceTypeUseCase
import com.omnimargen.omniserv.domain.usecase.serviceType.DeleteServiceTypeUseCase
import com.omnimargen.omniserv.domain.usecase.serviceType.GetAllServiceTypesUseCase
import com.omnimargen.omniserv.domain.usecase.serviceType.UpdateServiceTypeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceTypeViewModel @Inject constructor(
    private val getAllServiceTypesUseCase: GetAllServiceTypesUseCase,
    private val addServiceTypeUseCase: AddServiceTypeUseCase,
    private val updateServiceTypeUseCase: UpdateServiceTypeUseCase,
    private val deleteServiceTypeUseCase: DeleteServiceTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceTypeUiState())
    val uiState: StateFlow<ServiceTypeUiState> = _uiState.asStateFlow()

    init {
        loadServiceTypes()
    }

    private fun loadServiceTypes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getAllServiceTypesUseCase().collect { types ->
                _uiState.update { it.copy(serviceTypes = types, isLoading = false) }
            }
        }
    }

    fun addServiceType(serviceType: ServiceType) {
        viewModelScope.launch {
            addServiceTypeUseCase(serviceType)
        }
    }

    fun updateServiceType(serviceType: ServiceType) {
        viewModelScope.launch {
            updateServiceTypeUseCase(serviceType)
        }
    }

    fun deleteServiceType(serviceType: ServiceType) {
        viewModelScope.launch {
            deleteServiceTypeUseCase(serviceType)
        }
    }
}
