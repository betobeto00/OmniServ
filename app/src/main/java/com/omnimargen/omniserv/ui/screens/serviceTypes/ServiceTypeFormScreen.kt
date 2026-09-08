package com.omnimargen.omniserv.ui.screens.serviceTypes

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimargen.omniserv.domain.model.ServiceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceTypeFormScreen(
    serviceTypeId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: ServiceTypeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = serviceTypeId != null

    var nombre by remember { mutableStateOf("") }

    LaunchedEffect(serviceTypeId) {
        if (serviceTypeId != null) {
            val type = uiState.serviceTypes.find { it.id == serviceTypeId }
            type?.let {
                nombre = it.nombre
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Tipo" else "Nuevo Tipo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
                label = { Text("Nombre del tipo de servicio *") },
                placeholder = { Text("Ej: Limpieza de aire acondicionado") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        val serviceType = ServiceType(
                            id = serviceTypeId ?: 0,
                            nombre = nombre.trim()
                        )
                        if (isEditing) {
                            viewModel.updateServiceType(serviceType)
                        } else {
                            viewModel.addServiceType(serviceType)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank()
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Crear Tipo")
            }
        }
    }
}
