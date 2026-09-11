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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.omnimargen.omniserv.update.UpdateDialog
import com.omnimargen.omniserv.update.UpdateViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToServiceForm: () -> Unit = {},
    onNavigateToServiceDetail: (Long) -> Unit = {},
    onNavigateToLegal: () -> Unit = {},
    onNavigateToActivation: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val updateUiState by updateViewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateUiState.readyToInstall) {
        if (updateUiState.readyToInstall) {
            updateViewModel.installDownloadedApk()
        }
    }

    LaunchedEffect(updateUiState.updateAvailable) {
        if (updateUiState.updateAvailable) {
            showUpdateDialog = true
        }
    }

    val context = LocalContext.current
    LaunchedEffect(updateUiState.checkMessage) {
        updateUiState.checkMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            updateViewModel.clearCheckMessage()
        }
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
            Column {
                // Blue header image + TopAppBar (icon only, no title)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.header_fondo_azul),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentScale = ContentScale.Crop
                    )
                    TopAppBar(
                        title = {},
                        actions = {},
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                }

                // Sub-header blanco: nombre de seccion + trial badge + botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Iquierda: titulo + badge trial
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Inicio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (uiState.isTrial) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Trial ${uiState.trialDaysRemaining}d",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    IconButton(
                                        onClick = onNavigateToActivation,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = "Comprar licencia",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Derecha: botones
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        IconButton(onClick = { onNavigateToLegal() }) {
                            Icon(Icons.Default.Info, contentDescription = "Info Legal")
                        }
                        IconButton(onClick = { updateViewModel.checkForUpdate(notifyResult = true) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Buscar actualizaciones")
                        }
                    }
                }
            }
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
                    ServiceQuickCard(
                        service = service,
                        dateFormat = dateFormat,
                        onClick = { onNavigateToServiceDetail(service.id) }
                    )
                }
            }

            if (uiState.upcomingServices.isNotEmpty()) {
                item {
                    SectionTitle("Próximos Servicios")
                }
                items(uiState.upcomingServices) { service ->
                    ServiceQuickCard(
                        service = service,
                        dateFormat = dateFormat,
                        onClick = { onNavigateToServiceDetail(service.id) }
                    )
                }
            }

            if (uiState.pendingServices.isNotEmpty()) {
                item {
                    SectionTitle("Pendientes")
                }
                items(uiState.pendingServices.take(5)) { service ->
                    ServiceQuickCard(
                        service = service,
                        dateFormat = dateFormat,
                        onClick = { onNavigateToServiceDetail(service.id) }
                    )
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
fun ServiceQuickCard(
    service: Service,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
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
                if (service.clienteTelefono.isNotBlank()) {
                    Text(
                        text = service.clienteTelefono,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
