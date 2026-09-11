package com.omnimargen.omniserv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.domain.model.LicenseStatus
import com.omnimargen.omniserv.domain.model.ServiceStatus
import com.omnimargen.omniserv.domain.usecase.license.GetLicenseStatusUseCase
import com.omnimargen.omniserv.domain.usecase.service.GetPendingServicesUseCase
import com.omnimargen.omniserv.domain.usecase.service.GetServicesUseCase
import com.omnimargen.omniserv.domain.usecase.service.GetUpcomingServicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPendingServicesUseCase: GetPendingServicesUseCase,
    private val getUpcomingServicesUseCase: GetUpcomingServicesUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val getLicenseStatusUseCase: GetLicenseStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadLicenseStatus()
    }

    private fun loadLicenseStatus() {
        viewModelScope.launch {
            val status = getLicenseStatusUseCase()
            when (status) {
                is LicenseStatus.TrialPeriod -> _uiState.update {
                    it.copy(isTrial = true, trialDaysRemaining = status.daysRemaining)
                }
                is LicenseStatus.Valid -> _uiState.update {
                    it.copy(isTrial = false, trialDaysRemaining = 0)
                }
                is LicenseStatus.GracePeriod -> _uiState.update {
                    it.copy(isTrial = false, trialDaysRemaining = 0)
                }
                else -> _uiState.update {
                    it.copy(isTrial = false, trialDaysRemaining = 0)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getPendingServicesUseCase().collect { pending ->
                val totalAmount = pending.sumOf { it.monto }
                _uiState.update { it.copy(pendingServices = pending, totalPendingAmount = totalAmount) }
            }
        }

        viewModelScope.launch {
            getServicesUseCase().collect { allServices ->
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val tomorrow = Calendar.getInstance().apply {
                    time = today
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time

                val todayServices = allServices.filter { service ->
                    val serviceDate = service.fechaServicio
                    serviceDate.after(today) && serviceDate.before(tomorrow)
                }

                _uiState.update {
                    it.copy(
                        todayServices = todayServices,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            try {
                val upcoming = getUpcomingServicesUseCase()
                _uiState.update { it.copy(upcomingServices = upcoming) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
