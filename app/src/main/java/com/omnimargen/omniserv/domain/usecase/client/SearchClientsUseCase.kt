package com.omnimargen.omniserv.domain.usecase.client

import com.omnimargen.omniserv.data.repository.ClientRepository
import com.omnimargen.omniserv.domain.model.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchClientsUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    operator fun invoke(query: String): Flow<List<Client>> = clientRepository.search(query)
}
