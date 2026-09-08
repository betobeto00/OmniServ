package com.omnimargen.omniserv.domain.usecase.client

import com.omnimargen.omniserv.data.repository.ClientRepository
import com.omnimargen.omniserv.domain.model.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetClientsUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    operator fun invoke(): Flow<List<Client>> = clientRepository.getAll()
}
