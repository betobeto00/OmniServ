package com.omnimargen.omniserv.domain.usecase.service

import com.omnimargen.omniserv.data.repository.ServiceRepository
import com.omnimargen.omniserv.domain.model.Service
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetServicesByDateRangeUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    operator fun invoke(start: Long, end: Long): Flow<List<Service>> =
        serviceRepository.getByDateRange(start, end)
}
