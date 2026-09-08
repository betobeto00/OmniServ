package com.omnimargen.omniserv.domain.usecase.service

import com.omnimargen.omniserv.data.repository.ServiceRepository
import com.omnimargen.omniserv.domain.model.Service
import com.omnimargen.omniserv.domain.model.ServiceStatus
import javax.inject.Inject

class MarkServiceCompletedUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    suspend operator fun invoke(service: Service) {
        serviceRepository.update(service.copy(estado = ServiceStatus.REALIZADO))
    }
}
