package com.omnimargen.omniserv.domain.usecase.license

import com.omnimargen.omniserv.data.repository.LicenseRepository
import com.omnimargen.omniserv.domain.model.LicenseStatus
import javax.inject.Inject

class GetLicenseStatusUseCase @Inject constructor(
    private val validateLicenseUseCase: ValidateLicenseUseCase
) {
    suspend operator fun invoke(): LicenseStatus = validateLicenseUseCase()
}
