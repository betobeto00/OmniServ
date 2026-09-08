package com.omnimargen.omniserv.update

data class UpdateUiState(
    val isChecking: Boolean = false,
    val updateAvailable: Boolean = false,
    val updateInfo: UpdateInfo? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    /** true cuando la descarga terminó: la UI debe lanzar el instalador del APK. */
    val readyToInstall: Boolean = false,
    val error: String? = null
)
