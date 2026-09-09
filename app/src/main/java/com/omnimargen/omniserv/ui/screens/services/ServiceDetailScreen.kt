package com.omnimargen.omniserv.ui.screens.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimargen.omniserv.domain.model.ServiceStatus
import com.omnimargen.omniserv.ui.components.BannerTopBar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val service = uiState.services.find { it.id == serviceId }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            BannerTopBar(
                title = "Detalle del Servicio",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (service == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text("Servicio no encontrado")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.clienteNombre,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                if (service.clienteTelefono.isNotBlank()) {
                                    Text(
                                        text = service.clienteTelefono,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "$${String.format("%.2f", service.monto)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!service.numeroFactura.isNullOrBlank()) {
                            Text(
                                text = "Factura N°: ${service.numeroFactura}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = "Tipo: ${service.tipoServicio}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = "Fecha: ${dateFormat.format(service.fechaServicio)}",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (service.notas.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notas:",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = service.notas,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status change section
                Text(
                    text = "Cambiar Estado",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceStatus.entries.forEach { status ->
                        FilterChip(
                            selected = service.estado == status,
                            onClick = {
                                viewModel.updateServiceStatus(service, status)
                            },
                            label = { Text(formatStatus(status.name)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (status) {
                                    ServiceStatus.PENDIENTE -> MaterialTheme.colorScheme.secondaryContainer
                                    ServiceStatus.EN_PROGRESO -> MaterialTheme.colorScheme.tertiaryContainer
                                    ServiceStatus.REALIZADO -> MaterialTheme.colorScheme.primaryContainer
                                    ServiceStatus.CANCELADO -> MaterialTheme.colorScheme.errorContainer
                                }
                            )
                        )
                    }
                }

                if (service.operarios.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Operarios asignados",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    service.operarios.forEach { serviceOperator ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = serviceOperator.operatorNombre,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Pago: $${String.format("%.2f", serviceOperator.montoPago)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (serviceOperator.pagado) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (serviceOperator.pagado) "Pagado" else "Pendiente",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (serviceOperator.pagado) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                    Checkbox(
                                        checked = serviceOperator.pagado,
                                        onCheckedChange = {
                                            val nuevos = service.operarios.map { op ->
                                                if (op.operatorId == serviceOperator.operatorId) {
                                                    op.copy(pagado = !op.pagado)
                                                } else {
                                                    op
                                                }
                                            }
                                            viewModel.updateOperatorPayment(service.id, nuevos)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
