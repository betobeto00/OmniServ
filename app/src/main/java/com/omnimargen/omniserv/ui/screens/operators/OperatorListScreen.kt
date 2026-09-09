package com.omnimargen.omniserv.ui.screens.operators

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimargen.omniserv.data.local.dao.OperatorPaymentSummary
import com.omnimargen.omniserv.domain.model.Operator
import com.omnimargen.omniserv.ui.components.BannerTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorListScreen(
    onNavigateToForm: (Long?) -> Unit,
    viewModel: OperatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            BannerTopBar(title = "Operarios")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar operario")
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.operators) { operator ->
                    val summary = uiState.paymentSummary.find { it.operatorId == operator.id }
                    OperatorCard(
                        operator = operator,
                        summary = summary,
                        onToggleActive = { viewModel.toggleActive(operator) },
                        onEdit = { onNavigateToForm(operator.id) },
                        onDelete = { viewModel.deleteOperator(operator) }
                    )
                }
            }
        }
    }
}

@Composable
fun OperatorCard(
    operator: Operator,
    summary: OperatorPaymentSummary?,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = operator.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = operator.telefono,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (summary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pendiente: $${String.format("%.2f", summary.pendiente)} · Pagado: $${String.format("%.2f", summary.pagado)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (summary.pendiente > 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (operator.activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (operator.activo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
                Switch(
                    checked = operator.activo,
                    onCheckedChange = { onToggleActive() }
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
