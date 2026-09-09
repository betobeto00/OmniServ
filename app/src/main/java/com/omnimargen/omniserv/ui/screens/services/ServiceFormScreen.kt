package com.omnimargen.omniserv.ui.screens.services

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimargen.omniserv.domain.model.Service
import com.omnimargen.omniserv.domain.model.ServiceOperator
import com.omnimargen.omniserv.ui.components.BannerTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceFormScreen(
    serviceId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToServiceTypes: () -> Unit = {},
    viewModel: ServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = serviceId != null
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var clienteId by remember { mutableStateOf<Long?>(null) }
    var serviceTypeId by remember { mutableStateOf<Long?>(null) }
    var tipoServicio by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Date()) }
    // operatorId → monto a pagar (string del campo de texto)
    var selectedOperators by remember { mutableStateOf(mapOf<Long, String>()) }
    var expandedClient by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    LaunchedEffect(serviceId) {
        if (serviceId != null) {
            val service = uiState.services.find { it.id == serviceId }
            service?.let {
                clienteId = it.clienteId
                serviceTypeId = it.serviceTypeId
                tipoServicio = it.tipoServicio
                monto = it.monto.toString()
                notas = it.notas
                selectedDate = it.fechaServicio
                selectedOperators = it.operarios.associate { op ->
                    op.operatorId to (op.montoPago.takeIf { m -> m > 0 }?.toString() ?: "")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BannerTopBar(
                title = if (isEditing) "Editar Servicio" else "Nuevo Servicio",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedClient,
                onExpandedChange = { expandedClient = it }
            ) {
                OutlinedTextField(
                    value = uiState.clients.find { it.id == clienteId }?.nombre ?: "",
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    label = { Text("Cliente *") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedClient) }
                )
                ExposedDropdownMenu(
                    expanded = expandedClient,
                    onDismissRequest = { expandedClient = false }
                ) {
                    uiState.clients.forEach { client ->
                        DropdownMenuItem(
                            text = { Text(client.nombre) },
                            onClick = {
                                clienteId = client.id
                                expandedClient = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = if (serviceTypeId != null) {
                            uiState.serviceTypes.find { it.id == serviceTypeId }?.nombre ?: ""
                        } else {
                            tipoServicio
                        },
                        onValueChange = {
                            tipoServicio = it
                            serviceTypeId = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        label = { Text("Tipo de servicio *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedType) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        uiState.serviceTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.nombre) },
                                onClick = {
                                    serviceTypeId = type.id
                                    tipoServicio = type.nombre
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNavigateToServiceTypes,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Gestionar tipos",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = monto,
                onValueChange = { monto = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto *") },
                prefix = { Text("$") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { showDatePicker = true }) {
                Text("Fecha: ${dateFormat.format(selectedDate)}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Operarios:", style = MaterialTheme.typography.titleSmall)
            uiState.operators.filter { it.activo }.forEach { operator ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedOperators.containsKey(operator.id),
                        onCheckedChange = { checked ->
                            selectedOperators = if (checked) {
                                selectedOperators + (operator.id to "")
                            } else {
                                selectedOperators - operator.id
                            }
                        }
                    )
                    Text(
                        text = operator.nombre,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedOperators.containsKey(operator.id)) {
                        OutlinedTextField(
                            value = selectedOperators[operator.id] ?: "",
                            onValueChange = { value ->
                                selectedOperators = selectedOperators + (operator.id to value)
                            },
                            modifier = Modifier.width(110.dp),
                            label = { Text("Monto $") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notas") },
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (clienteId != null && tipoServicio.isNotBlank() && monto.toDoubleOrNull() != null) {
                        val service = Service(
                            id = serviceId ?: 0,
                            clienteId = clienteId!!,
                            serviceTypeId = serviceTypeId,
                            tipoServicio = tipoServicio.trim(),
                            fechaServicio = selectedDate,
                            monto = monto.toDouble(),
                            notas = notas.trim(),
                            operarios = selectedOperators.map { (opId, montoOperario) ->
                                val op = uiState.operators.find { it.id == opId }
                                ServiceOperator(
                                    operatorId = opId,
                                    operatorNombre = op?.nombre ?: "",
                                    montoPago = montoOperario.toDoubleOrNull() ?: 0.0
                                )
                            }
                        )
                        if (isEditing) {
                            viewModel.updateService(service)
                        } else {
                            viewModel.addService(service)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = clienteId != null && tipoServicio.isNotBlank() && monto.toDoubleOrNull() != null
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Crear Servicio")
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = millis
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY))
                        calendar.set(java.util.Calendar.MINUTE, java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE))
                        selectedDate = calendar.time
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
