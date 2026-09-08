package com.omnimargen.omniserv.domain.usecase.client

import com.omnimargen.omniserv.data.repository.ClientRepository
import com.omnimargen.omniserv.domain.model.Client
import javax.inject.Inject

class UpdateClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(client: Client) = clientRepository.update(client)
}
