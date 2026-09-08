package com.omnimargen.omniserv.domain.usecase.serviceType

import com.omnimargen.omniserv.data.repository.ServiceTypeRepository
import com.omnimargen.omniserv.domain.model.ServiceType
import javax.inject.Inject

class AddServiceTypeUseCase @Inject constructor(
    private val serviceTypeRepository: ServiceTypeRepository
) {
    suspend operator fun invoke(serviceType: ServiceType): Long =
        serviceTypeRepository.insert(serviceType)
}
