package com.omnimargen.omniserv.update

data class UpdateUiState(
    val isChecking: Boolean = false,
    val updateAvailable: Boolean = false,
    val updateInfo: UpdateInfo? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null
)
