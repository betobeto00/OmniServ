package com.omnimargen.omniserv.ui.screens.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnimargen.omniserv.domain.model.Client
import com.omnimargen.omniserv.domain.usecase.client.AddClientUseCase
import com.omnimargen.omniserv.domain.usecase.client.DeleteClientUseCase
import com.omnimargen.omniserv.domain.usecase.client.GetClientsUseCase
import com.omnimargen.omniserv.domain.usecase.client.SearchClientsUseCase
import com.omnimargen.omniserv.domain.usecase.client.UpdateClientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val getClientsUseCase: GetClientsUseCase,
    private val searchClientsUseCase: SearchClientsUseCase,
    private val addClientUseCase: AddClientUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    init {
        loadClients()
    }

    private fun loadClients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getClientsUseCase().collect { clients ->
                _uiState.update { it.copy(clients = clients, isLoading = false) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                getClientsUseCase().collect { clients ->
                    _uiState.update { it.copy(clients = clients) }
                }
            } else {
                searchClientsUseCase(query).collect { clients ->
                    _uiState.update { it.copy(clients = clients) }
                }
            }
        }
    }

    fun addClient(client: Client) {
        viewModelScope.launch {
            addClientUseCase(client)
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            updateClientUseCase(client)
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            deleteClientUseCase(client)
        }
    }
}
