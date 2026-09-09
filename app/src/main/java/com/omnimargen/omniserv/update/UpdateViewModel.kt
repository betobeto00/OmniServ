package com.omnimargen.omniserv.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateChecker: UpdateChecker,
    private val updateInstaller: UpdateInstaller
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    init {
        checkForUpdate()
    }

    /**
     * Verifica si hay una actualización disponible.
     * @param notifyResult true cuando el chequeo fue iniciado manualmente por el usuario:
     *   se rellena [UpdateUiState.checkMessage] con el resultado ("estás al día" o el error)
     *   para que la UI lo muestre de forma fiable. Los chequeos automáticos (init) no avisan.
     */
    fun checkForUpdate(notifyResult: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, error = null) }

            try {
                val updateInfo = updateChecker.checkForUpdate()
                _uiState.update {
                    it.copy(
                        isChecking = false,
                        updateAvailable = updateInfo != null,
                        updateInfo = updateInfo,
                        checkMessage = if (notifyResult && updateInfo == null) {
                            "Estás en la última versión"
                        } else {
                            null
                        }
                    )
                }
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("timeout", true) == true -> "Sin conexión. Verifica tu red."
                    e.message?.contains("HTTP 403", true) == true -> "Límite de solicitudes de GitHub. Intenta más tarde."
                    e.message?.contains("HTTP 429", true) == true -> "Demasiadas solicitudes. Espera unos minutos."
                    e.message?.contains("Unable to resolve", true) == true -> "Sin acceso a internet."
                    e.message?.contains("HTTP", true) == true -> e.message
                    else -> "Error: ${e.localizedMessage ?: e.javaClass.simpleName}"
                }
                _uiState.update {
                    it.copy(
                        isChecking = false,
                        error = msg,
                        checkMessage = if (notifyResult) msg else null
                    )
                }
            }
        }
    }

    fun startDownload() {
        val updateInfo = _uiState.value.updateInfo ?: return

        _uiState.update { it.copy(isDownloading = true, downloadProgress = 0) }

        viewModelScope.launch {
            // DownloadManager escribe el APK en disco; esperar a que exista evita
            // lanzar el instalador con un archivo a medio escribir.
            val file = updateInstaller.getTargetFile(updateInfo)
            while (!file.exists() || file.length() == 0L) {
                delay(500)
            }
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    readyToInstall = true,
                    downloadProgress = 100
                )
            }
        }
    }

    /** Lanza el instalador de Android con el APK descargado (idempotente). */
    fun installDownloadedApk() {
        val updateInfo = _uiState.value.updateInfo ?: return
        val file = updateInstaller.getTargetFile(updateInfo)
        if (file.exists()) {
            _uiState.update { it.copy(readyToInstall = false) }
            updateInstaller.installApk(file)
        }
    }

    fun dismissUpdate() {
        _uiState.update {
            it.copy(
                updateAvailable = false,
                updateInfo = null,
                isDownloading = false,
                readyToInstall = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /** Consume el mensaje de check manual tras mostrarlo (p. ej. un Toast). */
    fun clearCheckMessage() {
        _uiState.update { it.copy(checkMessage = null) }
    }
}
