package com.omnimargen.omniserv.domain.usecase.serviceType

import com.omnimargen.omniserv.data.repository.ServiceTypeRepository
import com.omnimargen.omniserv.domain.model.ServiceType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllServiceTypesUseCase @Inject constructor(
    private val serviceTypeRepository: ServiceTypeRepository
) {
    operator fun invoke(): Flow<List<ServiceType>> = serviceTypeRepository.getAll()
}
