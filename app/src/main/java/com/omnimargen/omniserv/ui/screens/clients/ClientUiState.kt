package com.omnimargen.omniserv.ui.screens.clients

import com.omnimargen.omniserv.domain.model.Client

data class ClientUiState(
    val clients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
