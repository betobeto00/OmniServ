package com.omnimargen.omniserv.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    fun checkForUpdate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, error = null) }

            try {
                val updateInfo = updateChecker.checkForUpdate()
                _uiState.update {
                    it.copy(
                        isChecking = false,
                        updateAvailable = updateInfo != null,
                        updateInfo = updateInfo
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isChecking = false,
                        error = "Error al verificar actualizaciones"
                    )
                }
            }
        }
    }

    fun startDownload() {
        val updateInfo = _uiState.value.updateInfo ?: return

        _uiState.update { it.copy(isDownloading = true, downloadProgress = 0) }

        updateInstaller.downloadAndInstall(updateInfo) { progress ->
            _uiState.update { it.copy(downloadProgress = progress) }
        }
    }

    fun dismissUpdate() {
        _uiState.update {
            it.copy(
                updateAvailable = false,
                updateInfo = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
