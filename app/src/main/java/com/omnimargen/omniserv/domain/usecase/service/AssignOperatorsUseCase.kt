package com.omnimargen.omniserv.domain.usecase.service

import com.omnimargen.omniserv.data.repository.ServiceRepository
import com.omnimargen.omniserv.domain.model.ServiceOperator
import javax.inject.Inject

class AssignOperatorsUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    suspend operator fun invoke(serviceId: Long, operators: List<ServiceOperator>) {
        serviceRepository.assignOperators(serviceId, operators)
    }
}
