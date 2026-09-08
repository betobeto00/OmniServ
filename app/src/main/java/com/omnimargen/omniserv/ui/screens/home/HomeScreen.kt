package com.omnimargen.omniserv.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimargen.omniserv.R
import com.omnimargen.omniserv.domain.model.Service
import com.omnimargen.omniserv.ui.components.BannerTopBar
import com.omnimargen.omniserv.update.UpdateDialog
import com.omnimargen.omniserv.update.UpdateViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToServiceForm: () -> Unit = {},
    onNavigateToLegal: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateUiState by updateViewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    var showUpdateDialog by remember { mutableStateOf(false) }

    // Descarga terminada → lanzar el instalador de Android automáticamente
    LaunchedEffect(updateUiState.readyToInstall) {
        if (updateUiState.readyToInstall) {
            updateViewModel.installDownloadedApk()
        }
    }

    // Si hay actualización disponible, mostrar el diálogo (una sola vez)
    LaunchedEffect(updateUiState.updateAvailable) {
        if (updateUiState.updateAvailable) {
            showUpdateDialog = true
        }
    }

    // Al terminar un chequeo: cerrar el diálogo si no hay nada nuevo y avisar
    // al usuario (el botón refrescar nunca debe quedarse "sin hacer nada").
    var wasChecking by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(updateUiState.isChecking) {
        if (wasChecking && !updateUiState.isChecking) {
            when {
                updateUiState.updateAvailable -> Unit // el diálogo se abre con el otro efecto
                updateUiState.error != null ->
                    Toast.makeText(context, updateUiState.error, Toast.LENGTH_SHORT).show()
                else -> {
                    showUpdateDialog = false
                    Toast.makeText(context, "Estás en la última versión", Toast.LENGTH_SHORT).show()
                }
            }
        }
        wasChecking = updateUiState.isChecking
    }

    if (showUpdateDialog && updateUiState.updateInfo != null) {
        UpdateDialog(
            updateInfo = updateUiState.updateInfo!!,
            isDownloading = updateUiState.isDownloading,
            downloadProgress = updateUiState.downloadProgress,
            readyToInstall = updateUiState.readyToInstall,
            onConfirm = {
                when {
                    updateUiState.readyToInstall -> updateViewModel.installDownloadedApk()
                    updateUiState.isDownloading -> Unit
                    else -> updateViewModel.startDownload()
                }
            },
            onDismiss = {
                showUpdateDialog = false
                updateViewModel.dismissUpdate()
            }
        )
    }

    Scaffold(
        topBar = {
            BannerTopBar(
                title = "",
                actions = {
                    IconButton(onClick = { onNavigateToLegal() }) {
                        Icon(Icons.Default.Info, contentDescription = "Info Legal")
                    }
                    IconButton(onClick = { updateViewModel.checkForUpdate() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Buscar actualizaciones")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToServiceForm,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo servicio")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SummaryCard(
                    pendingCount = uiState.pendingServices.size,
                    totalPending = uiState.totalPendingAmount
                )
            }

            if (updateUiState.updateAvailable) {
                item {
                    UpdateAvailableCard(
                        versionName = updateUiState.updateInfo?.versionName ?: "",
                        onUpdateClick = { showUpdateDialog = true }
                    )
                }
            }

            if (uiState.todayServices.isNotEmpty()) {
                item {
                    SectionTitle("Servicios de Hoy")
                }
                items(uiState.todayServices) { service ->
                    ServiceQuickCard(service = service, dateFormat = dateFormat)
                }
            }

            if (uiState.upcomingServices.isNotEmpty()) {
                item {
                    SectionTitle("Próximos Servicios")
                }
                items(uiState.upcomingServices) { service ->
                    ServiceQuickCard(service = service, dateFormat = dateFormat)
                }
            }

            if (uiState.pendingServices.isNotEmpty()) {
                item {
                    SectionTitle("Pendientes")
                }
                items(uiState.pendingServices.take(5)) { service ->
                    ServiceQuickCard(service = service, dateFormat = dateFormat)
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun UpdateAvailableCard(versionName: String, onUpdateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onUpdateClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Actualización disponible",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Versión $versionName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Text(
                text = "Actualizar",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SummaryCard(pendingCount: Int, totalPending: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Pendientes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$pendingCount servicios",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$${String.format("%.2f", totalPending)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ServiceQuickCard(service: Service, dateFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.clienteNombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = service.tipoServicio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dateFormat.format(service.fechaServicio),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$${String.format("%.2f", service.monto)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
