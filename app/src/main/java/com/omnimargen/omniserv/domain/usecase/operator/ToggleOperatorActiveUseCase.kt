package com.omnimargen.omniserv.domain.usecase.operator

import com.omnimargen.omniserv.data.repository.OperatorRepository
import com.omnimargen.omniserv.domain.model.Operator
import javax.inject.Inject

class ToggleOperatorActiveUseCase @Inject constructor(
    private val operatorRepository: OperatorRepository
) {
    suspend operator fun invoke(operator: Operator) {
        operatorRepository.update(operator.copy(activo = !operator.activo))
    }
}
