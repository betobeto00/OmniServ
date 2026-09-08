package com.omnimargen.omniserv.domain.usecase.operator

import com.omnimargen.omniserv.data.repository.OperatorRepository
import com.omnimargen.omniserv.domain.model.Operator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOperatorsUseCase @Inject constructor(
    private val operatorRepository: OperatorRepository
) {
    operator fun invoke(): Flow<List<Operator>> = operatorRepository.getAll()
}
