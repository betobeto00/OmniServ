package com.omnimargen.omniserv.ui.screens.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimargen.omniserv.domain.model.Service
import com.omnimargen.omniserv.domain.model.ServiceStatus
import com.omnimargen.omniserv.ui.components.BannerTopBar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    onNavigateToForm: (Long?) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: ServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            BannerTopBar(title = "Servicios")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar servicio")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filterStatus == null,
                    onClick = { viewModel.setFilterStatus(null) },
                    label = { Text("Todos") }
                )
                ServiceStatus.entries.forEach { status ->
                    FilterChip(
                        selected = uiState.filterStatus == status,
                        onClick = { viewModel.setFilterStatus(status) },
                        label = { Text(formatStatus(status.name), fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredServices = if (uiState.filterStatus != null) {
                uiState.services.filter { it.estado == uiState.filterStatus }
            } else {
                uiState.services
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredServices) { service ->
                    ServiceCard(
                        service = service,
                        dateFormat = dateFormat,
                        onDetail = { onNavigateToDetail(service.id) },
                        onEdit = { onNavigateToForm(service.id) },
                        onDelete = { viewModel.deleteService(service) }
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: Service,
    dateFormat: SimpleDateFormat,
    onDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.clienteNombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = service.tipoServicio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "$${String.format("%.2f", service.monto)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(service.fechaServicio),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
                Text(
                    text = formatStatus(service.estado.name),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = when (service.estado) {
                        ServiceStatus.PENDIENTE -> MaterialTheme.colorScheme.secondary
                        ServiceStatus.EN_PROGRESO -> MaterialTheme.colorScheme.tertiary
                        ServiceStatus.REALIZADO -> MaterialTheme.colorScheme.primary
                        ServiceStatus.CANCELADO -> MaterialTheme.colorScheme.error
                    }
                )
            }

            if (service.operarios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ops: ${service.operarios.joinToString { it.operatorNombre }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDetail) {
                    Icon(Icons.Default.Info, contentDescription = "Detalle", modifier = Modifier.height(18.dp))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.height(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.height(18.dp)
                    )
                }
            }
        }
    }
}

fun formatStatus(status: String): String {
    return when (status) {
        "PENDIENTE" -> "Pendiente"
        "EN_PROGRESO" -> "En Progreso"
        "REALIZADO" -> "Realizado"
        "CANCELADO" -> "Cancelado"
        else -> status
    }
}
