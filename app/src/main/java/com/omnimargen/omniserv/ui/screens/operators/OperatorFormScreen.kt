package com.omnimargen.omniserv.ui.screens.operators

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.omnimargen.omniserv.domain.model.Operator
import com.omnimargen.omniserv.ui.components.BannerTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorFormScreen(
    operatorId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: OperatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = operatorId != null

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }

    LaunchedEffect(operatorId) {
        if (operatorId != null) {
            val operator = uiState.operators.find { it.id == operatorId }
            operator?.let {
                nombre = it.nombre
                telefono = it.telefono
                activo = it.activo
            }
        }
    }

    Scaffold(
        topBar = {
            BannerTopBar(
                title = if (isEditing) "Editar Operario" else "Nuevo Operario",
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
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre completo *") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Teléfono *") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Switch(
                checked = activo,
                onCheckedChange = { activo = it }
            )
            Text(
                text = if (activo) "Activo" else "Inactivo",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && telefono.isNotBlank()) {
                        val operator = Operator(
                            id = operatorId ?: 0,
                            nombre = nombre.trim(),
                            telefono = telefono.trim(),
                            activo = activo
                        )
                        if (isEditing) {
                            viewModel.updateOperator(operator)
                        } else {
                            viewModel.addOperator(operator)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank() && telefono.isNotBlank()
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Crear Operario")
            }
        }
    }
}
