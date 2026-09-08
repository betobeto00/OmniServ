package com.omnimargen.omniserv.domain.usecase.service

import com.omnimargen.omniserv.data.repository.ServiceRepository
import com.omnimargen.omniserv.domain.model.Service
import javax.inject.Inject

class AddServiceUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    suspend operator fun invoke(service: Service): Long = serviceRepository.insert(service)
}
