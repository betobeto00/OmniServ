package com.omnimargen.omniserv.ui.screens.operators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.domain.model.Operator
import com.omnimargen.omniserv.domain.usecase.operator.AddOperatorUseCase
import com.omnimargen.omniserv.domain.usecase.operator.DeleteOperatorUseCase
import com.omnimargen.omniserv.domain.usecase.operator.GetOperatorsUseCase
import com.omnimargen.omniserv.domain.usecase.operator.ToggleOperatorActiveUseCase
import com.omnimargen.omniserv.domain.usecase.operator.UpdateOperatorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OperatorViewModel @Inject constructor(
    private val getOperatorsUseCase: GetOperatorsUseCase,
    private val addOperatorUseCase: AddOperatorUseCase,
    private val updateOperatorUseCase: UpdateOperatorUseCase,
    private val deleteOperatorUseCase: DeleteOperatorUseCase,
    private val toggleOperatorActiveUseCase: ToggleOperatorActiveUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorUiState())
    val uiState: StateFlow<OperatorUiState> = _uiState.asStateFlow()

    init {
        loadOperators()
    }

    private fun loadOperators() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getOperatorsUseCase().collect { operators ->
                _uiState.update { it.copy(operators = operators, isLoading = false) }
            }
        }
    }

    fun addOperator(operator: Operator) {
        viewModelScope.launch {
            addOperatorUseCase(operator)
        }
    }

    fun updateOperator(operator: Operator) {
        viewModelScope.launch {
            updateOperatorUseCase(operator)
        }
    }

    fun deleteOperator(operator: Operator) {
        viewModelScope.launch {
            deleteOperatorUseCase(operator)
        }
    }

    fun toggleActive(operator: Operator) {
        viewModelScope.launch {
            toggleOperatorActiveUseCase(operator)
        }
    }
}
