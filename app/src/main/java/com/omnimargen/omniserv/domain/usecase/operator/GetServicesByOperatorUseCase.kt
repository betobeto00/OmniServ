package com.omnimargen.omniserv.domain.usecase.operator

import com.omnimargen.omniserv.data.repository.ServiceRepository
import com.omnimargen.omniserv.domain.model.Service
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetServicesByOperatorUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    operator fun invoke(operatorId: Long): Flow<List<Service>> =
        serviceRepository.getServicesByOperatorId(operatorId)
}
