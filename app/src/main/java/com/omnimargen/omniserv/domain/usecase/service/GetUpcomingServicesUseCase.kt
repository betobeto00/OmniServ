package com.omnimargen.omniserv.domain.usecase.service

import com.omnimargen.omniserv.data.repository.ServiceRepository
import com.omnimargen.omniserv.domain.model.Service
import javax.inject.Inject

class GetUpcomingServicesUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    suspend operator fun invoke(): List<Service> {
        val now = System.currentTimeMillis()
        val twoDaysFromNow = now + (2 * 24 * 60 * 60 * 1000)
        return serviceRepository.getUpcoming(now, twoDaysFromNow)
    }
}
