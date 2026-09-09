package com.omnimargen.omniserv.domain.usecase.operator

import com.omnimargen.omniserv.data.local.dao.OperatorPaymentSummary
import com.omnimargen.omniserv.data.repository.ServiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOperatorPaymentSummaryUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    operator fun invoke(): Flow<List<OperatorPaymentSummary>> =
        serviceRepository.getOperatorPaymentSummary()
}